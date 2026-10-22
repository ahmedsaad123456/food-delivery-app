package com.example.food_delivery_app.restaurant.service;

import com.example.food_delivery_app.exceptions.ResourceNotFoundException;
import com.example.food_delivery_app.restaurant.dto.CreateRestaurantRequest;
import com.example.food_delivery_app.restaurant.dto.RestaurantMapper;
import com.example.food_delivery_app.restaurant.dto.RestaurantResponse;
import com.example.food_delivery_app.restaurant.entity.Category;
import com.example.food_delivery_app.restaurant.entity.Restaurant;
import com.example.food_delivery_app.restaurant.repository.CategoryRepository;
import com.example.food_delivery_app.restaurant.repository.RestaurantRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final CategoryRepository categoryRepository;
    private final RestaurantMapper mapper;


    private static final String RESTAURANT_NOT_FOUND = "Restaurant";

    @Transactional
    public RestaurantResponse create(CreateRestaurantRequest req) {
        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", req.categoryId()));

        Restaurant saved = restaurantRepository.save(mapper.toEntity(req, category));
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)

    public RestaurantResponse getById(Integer id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESTAURANT_NOT_FOUND, id));
        return mapper.toResponse(restaurant);
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> getAllRestaurants() {
        return mapper.toListResponse(
                restaurantRepository.findAll()
        );
    }

    @Transactional
    public RestaurantResponse updateRestaurant(Integer id, CreateRestaurantRequest req) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESTAURANT_NOT_FOUND, id));

        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", req.categoryId()));

        restaurant.setName(req.name());
        restaurant.setDescription(req.description());
        restaurant.setAddress(req.address());
        restaurant.setPhone(req.phone());
        restaurant.setOpen(req.open());
        restaurant.setCategory(category);

        Restaurant updated = restaurantRepository.save(restaurant);
        return mapper.toResponse(updated);
    }

    @Transactional
    public void deleteRestaurant(Integer id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESTAURANT_NOT_FOUND, id));
        restaurantRepository.delete(restaurant);
    }
}