package com.example.food_delivery_app.restaurant.dto.restaurant;


import java.time.Instant;
import java.util.Map;

public record ReviewResponse(String id, Integer restaurantId, Integer userId, int rating,
                             String comment, Instant createdAt, Map<String, Object> extras) {}