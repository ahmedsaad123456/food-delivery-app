package com.example.food_delivery_app.features.restaurant.dto;



import java.time.Instant;

public record RestaurantResponse(
        Long id,
        String name,
        String description,
        Long categoryId,
        String categoryName,
        String address,
        String phone,
        boolean open,
        Instant createdAt
) {}