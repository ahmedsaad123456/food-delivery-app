package com.example.food_delivery_app.restaurant.security;

import com.example.food_delivery_app.restaurant.repository.MenuItemRepository;
import com.example.food_delivery_app.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("restaurantSecurity")
@RequiredArgsConstructor
public class RestaurantSecurity {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    public boolean isOwner(Integer restaurantId, Authentication auth) {
        return restaurantRepository.existsByIdAndOwnerEmail(restaurantId, auth.getName());
    }

    public boolean isMenuItemOwner(Integer menuItemId, Authentication auth) {
        return menuItemRepository.existsByIdAndRestaurantOwnerEmail(menuItemId, auth.getName());
    }
}