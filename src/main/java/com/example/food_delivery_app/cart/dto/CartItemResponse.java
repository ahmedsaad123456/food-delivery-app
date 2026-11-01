package com.example.food_delivery_app.cart.dto;

import com.example.food_delivery_app.common.Money;

public record CartItemResponse(Integer menuItemId, String name, Money unitPrice, int quantity, Money lineTotal) {}
