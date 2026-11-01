package com.example.food_delivery_app.cart.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddToCartRequest(
        @NotNull Integer menuItemId,
        @Min(1) @Max(99) int quantity) {}