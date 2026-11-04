package com.example.food_delivery_app.restaurant.dto.restaurant;

public interface RestaurantSummary {
    Integer getId();
    String getName();
    String getAddress();
    boolean isOpen();
    CategoryView getCategory();

    interface CategoryView {
        Integer getId();
        String getName();
    }
}