package com.example.food_delivery_app.cart.dto;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

// 0 means "remove this item"
public record UpdateQuantityRequest(@Min(0) @Max(99) int quantity) {}
