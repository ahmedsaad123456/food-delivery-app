package com.example.food_delivery_app.menu.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record MenuItemResponse(
        Integer id,
        Integer restaurantId,
        String section,
        String name,
        String description,
        BigDecimal price,
        boolean available,
        Instant createdAt
) {}