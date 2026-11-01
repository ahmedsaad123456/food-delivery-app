package com.example.food_delivery_app.cart.dto;

import com.example.food_delivery_app.common.Money;

import java.util.List;

public record CartResponse(List<CartItemResponse> items, Money subtotal) {}

