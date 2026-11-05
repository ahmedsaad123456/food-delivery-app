package com.example.food_delivery_app.order.dto.order;

import com.example.food_delivery_app.common.Money;

public record OrderItemResponse(Long menuItemId, String name, Money unitPrice,
                                int quantity, Money lineTotal) {}