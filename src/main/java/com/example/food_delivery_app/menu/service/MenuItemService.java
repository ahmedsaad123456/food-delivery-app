package com.example.food_delivery_app.menu.service;

import com.example.food_delivery_app.common.Constants;
import com.example.food_delivery_app.common.ErrorCode;
import com.example.food_delivery_app.exceptions.AppException;
import com.example.food_delivery_app.exceptions.ResourceNotFoundException;
import com.example.food_delivery_app.menu.dto.CreateMenuItemRequest;
import com.example.food_delivery_app.menu.dto.MenuItemResponse;
import com.example.food_delivery_app.menu.dto.MenuSectionResponse;
import com.example.food_delivery_app.menu.dto.UpdateMenuItemRequest;
import com.example.food_delivery_app.menu.entity.MenuItem;
import com.example.food_delivery_app.menu.mapper.MenuItemMapper;
import com.example.food_delivery_app.menu.repository.MenuItemRepository;
import com.example.food_delivery_app.restaurant.entity.Restaurant;
import com.example.food_delivery_app.restaurant.repository.RestaurantRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemMapper mapper;


    // cacheable: if the same restaurantId is requested again, return the cached result instead of querying the database
    @Cacheable(cacheNames = Constants.MENU_CACHE, key = "#restaurantId")
    @Transactional(readOnly = true)
    public List<MenuItemResponse> findMenuItemsByRestaurant(Integer restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new ResourceNotFoundException(Constants.RESTAURANT, restaurantId);
        }
        return menuItemRepository.findByRestaurantId(restaurantId).stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MenuItemResponse findMenuItemById(Integer id) {
        return mapper.toResponse(getOrThrow(id));
    }


    // cacheEvict: when a menu item is created, updated, or deleted, the cache for that restaurantId is invalidated so that the next request will fetch fresh data from the database
    @CacheEvict(cacheNames = Constants.MENU_CACHE, key = "#restaurantId")
    @Transactional
    public MenuItemResponse createMenuItem(Integer restaurantId, CreateMenuItemRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.RESTAURANT, restaurantId));
        MenuItem item = mapper.toEntity(request);
        item.setRestaurant(restaurant);
        return mapper.toResponse(menuItemRepository.save(item));
    }

    @CacheEvict(cacheNames = Constants.MENU_CACHE, allEntries = true)
    @Transactional
    public MenuItemResponse updateMenuItem(Integer id, UpdateMenuItemRequest request) {
        MenuItem item = getOrThrow(id);
        mapper.update(request, item);                     // no save() needed (dirty checking)
        return mapper.toResponse(item);
    }

    @CacheEvict(cacheNames = Constants.MENU_CACHE, allEntries = true)
    @Transactional
    public void deleteMenuItem(Integer id) {
        if (!menuItemRepository.existsById(id)) {
            throw new ResourceNotFoundException(Constants.MENU_ITEM, id);
        }
        menuItemRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<MenuSectionResponse> menuGroupedBySection(Integer restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, Constants.RESTAURANT, restaurantId);
        }
        return menuItemRepository.findByRestaurantIdOrderBySectionAscNameAsc(restaurantId).stream()
                .map(mapper::toResponse) // map MenuItem to MenuItemResponse
                .collect(Collectors.groupingBy(
                        MenuItemResponse::section, LinkedHashMap::new, Collectors.toList())) // group by section while preserving order
                .entrySet().stream() // convert the map to a stream of entries
                .map(e -> new MenuSectionResponse(e.getKey(), e.getValue())) // map each entry to a MenuSectionResponse
                .toList();
    }

    private MenuItem getOrThrow(Integer id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.MENU_ITEM, id));
    }


}