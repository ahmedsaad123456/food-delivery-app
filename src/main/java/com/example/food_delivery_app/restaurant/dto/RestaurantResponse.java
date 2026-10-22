package com.example.food_delivery_app.restaurant.dto;



import lombok.Builder;

import java.time.Instant;


@Builder
public record RestaurantResponse(
        Integer id,
        String name,
        String description,
        Integer categoryId,
        String categoryName,
        String address,
        String phone,
        boolean open,
        Instant createdAt
) {}