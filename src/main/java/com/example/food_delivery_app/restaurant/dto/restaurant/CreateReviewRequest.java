package com.example.food_delivery_app.restaurant.dto.restaurant;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record CreateReviewRequest(
        @Min(1) @Max(5) int rating,
        @Size(max = 2000) String comment,
        @Size(max = 10) Map<String, Object> extras) {}