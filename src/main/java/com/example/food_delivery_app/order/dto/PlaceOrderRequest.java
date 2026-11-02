package com.example.food_delivery_app.order.dto;

import com.example.food_delivery_app.cart.pricing.discount_strategy.DiscountType;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;


// user id is not included in the request because it will be retrieved from the security context (the authenticated user).
public record PlaceOrderRequest(
        DiscountType discountType,                   // optional
        @PositiveOrZero BigDecimal discountValue     // optional, never negative
) {}