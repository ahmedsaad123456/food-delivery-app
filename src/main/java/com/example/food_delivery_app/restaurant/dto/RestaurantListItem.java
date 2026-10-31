package com.example.food_delivery_app.restaurant.dto;

public record RestaurantListItem(Integer id, String name, String address, boolean open,
                                 Integer categoryId, String categoryName) {
    public static RestaurantListItem from(RestaurantSummary s) {
        return new RestaurantListItem(s.getId(), s.getName(), s.getAddress(), s.isOpen(),
                s.getCategory().getId(), s.getCategory().getName());
    }
}