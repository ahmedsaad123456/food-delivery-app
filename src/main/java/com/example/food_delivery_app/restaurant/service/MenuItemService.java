package com.example.food_delivery_app.restaurant.service;

import com.example.food_delivery_app.common.Constants;
import com.example.food_delivery_app.common.ErrorCode;
import com.example.food_delivery_app.exceptions.AppException;
import com.example.food_delivery_app.exceptions.ResourceNotFoundException;
import com.example.food_delivery_app.restaurant.dto.menu_item.CreateMenuItemRequest;
import com.example.food_delivery_app.restaurant.dto.menu_item.MenuItemResponse;
import com.example.food_delivery_app.restaurant.dto.menu_item.MenuSectionResponse;
import com.example.food_delivery_app.restaurant.dto.menu_item.UpdateMenuItemRequest;
import com.example.food_delivery_app.restaurant.entity.MenuItem;
import com.example.food_delivery_app.restaurant.mapper.MenuItemMapper;
import com.example.food_delivery_app.restaurant.repository.MenuItemRepository;
import com.example.food_delivery_app.order.entity.order.Order;
import com.example.food_delivery_app.order.entity.order.OrderItem;
import com.example.food_delivery_app.restaurant.entity.Restaurant;
import com.example.food_delivery_app.restaurant.repository.RestaurantRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
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
        return menuItemRepository.findByRestaurantId(restaurantId).stream().map(mapper::toMenuItemResponse).toList();
    }

    @Transactional(readOnly = true)
    public MenuItemResponse findMenuItemById(Integer id) {
        return mapper.toMenuItemResponse(getMenuItemOrThrow(id));
    }


    // cacheEvict: when a menu item is created, updated, or deleted, the cache for that restaurantId is invalidated so that the next request will fetch fresh data from the database
    @CacheEvict(cacheNames = Constants.MENU_CACHE, key = "#restaurantId")
    @Transactional
    public MenuItemResponse createMenuItem(Integer restaurantId, CreateMenuItemRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.RESTAURANT, restaurantId));
        MenuItem item = mapper.toMenuItemEntity(request);
        item.setRestaurant(restaurant);
        return mapper.toMenuItemResponse(menuItemRepository.save(item));
    }

    @CacheEvict(cacheNames = Constants.MENU_CACHE, allEntries = true)
    @Transactional
    public MenuItemResponse updateMenuItem(Integer id, UpdateMenuItemRequest request) {
        MenuItem item = getMenuItemOrThrow(id);
        mapper.updateMenuItem(request, item);                     // no save() needed (dirty checking)
        return mapper.toMenuItemResponse(item);
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
                .map(mapper::toMenuItemResponse) // map MenuItem to MenuItemResponse
                .collect(Collectors.groupingBy(
                        MenuItemResponse::section, LinkedHashMap::new, Collectors.toList())) // group by section while preserving order
                .entrySet().stream() // convert the map to a stream of entries
                .map(e -> new MenuSectionResponse(e.getKey(), e.getValue())) // map each entry to a MenuSectionResponse
                .toList();
    }

    private MenuItem getMenuItemOrThrow(Integer id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.MENU_ITEM, id));
    }

    // Used BEFORE payment: throws a clear 409 if any item has less stock than the order needs.
    @Transactional(readOnly = true)
    public void assertItemsAvailableFromOrder(Order order) {

        // Get all menu items in the order in one query, and map them by id for easy lookup
        Map<Integer, MenuItem> current = menuItemRepository
                .findAllById(order.getItems().stream().map(OrderItem::getMenuItemId).toList())
                .stream()
                .collect(Collectors.toMap(MenuItem::getId, Function.identity()));

        // Check each item in the order against the current stock
        for (OrderItem item : order.getItems()) {
            MenuItem menuItem = current.get(item.getMenuItemId());
            boolean missing = menuItem == null;
            // stock == null means unlimited
            boolean notEnough = menuItem != null && menuItem.getStock() != null
                    && menuItem.getStock() < item.getQuantity();
            if (missing || notEnough) {
                throw new AppException(ErrorCode.INSUFFICIENT_STOCK, item.getName());
            }
        }


    }

    // Used AFTER payment, inside the webhook transaction.
    // All-or-nothing for ONE order: returns false (and leaves the stock exactly as it was) if any item is short.
    @Transactional
    public boolean tryDeductItemsFromStock(Order order) {
        List<OrderItem> items = order.getItems().stream()
                .sorted(Comparator.comparing(OrderItem::getMenuItemId))
                .toList();

        List<OrderItem> deducted = new ArrayList<>();
        for (OrderItem item : items) {
            if (menuItemRepository.deductStock(item.getMenuItemId(), item.getQuantity()) == 1) {
                deducted.add(item);
            } else {
                // This item is short: put back what we already took from the previous ones
                deducted.forEach(d -> menuItemRepository.addStock(d.getMenuItemId(), d.getQuantity()));
                return false;
            }
        }
        return true;
    }

    // check if the menu item is available for order (stock > 0 or stock is null)
    @Transactional(readOnly = true)
    public void assertAvailableMenuItem(Integer menuItemId , Integer quantity) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.MENU_ITEM, menuItemId));
        if (menuItem.getStock() != null && menuItem.getStock() < quantity) {
            throw new AppException(
                    ErrorCode.INSUFFICIENT_STOCK,
                    menuItem.getName()
            );
        }
    }


}