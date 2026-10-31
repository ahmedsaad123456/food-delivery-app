package com.example.food_delivery_app.restaurant.service;

import com.example.food_delivery_app.common.Constants;
import com.example.food_delivery_app.exceptions.ResourceNotFoundException;
import com.example.food_delivery_app.restaurant.dto.CreateRestaurantRequest;
import com.example.food_delivery_app.restaurant.mapper.RestaurantMapper;
import com.example.food_delivery_app.restaurant.dto.RestaurantResponse;
import com.example.food_delivery_app.restaurant.entity.Category;
import com.example.food_delivery_app.restaurant.entity.Restaurant;
import com.example.food_delivery_app.restaurant.repository.CategoryRepository;
import com.example.food_delivery_app.restaurant.repository.RestaurantRepository;
import com.example.food_delivery_app.users.entity.User;
import com.example.food_delivery_app.users.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RestaurantMapper mapper;



    @Transactional
    public RestaurantResponse createRestaurant(CreateRestaurantRequest req , String ownerEmail) {
        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException(Constants.CATEGORY, req.categoryId()));

        // service.create(request, String ownerEmail)
        User owner = userRepository.findByEmail(ownerEmail).orElseThrow(() -> new ResourceNotFoundException(Constants.USER, ownerEmail));
        var restaurant = mapper.toEntity(req, category);
        restaurant.setOwner(owner);
        Restaurant saved = restaurantRepository.save(restaurant);
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)

    public RestaurantResponse getRestaurantById(Integer id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.RESTAURANT, id));
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
                .orElseThrow(() -> new ResourceNotFoundException(Constants.RESTAURANT, id));

        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException(Constants.CATEGORY, req.categoryId()));

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
                .orElseThrow(() -> new ResourceNotFoundException(Constants.RESTAURANT, id));
        restaurantRepository.delete(restaurant);
    }
}