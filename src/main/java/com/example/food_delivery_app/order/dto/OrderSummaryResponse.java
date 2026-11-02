package com.example.food_delivery_app.order.dto;

import com.example.food_delivery_app.common.Money;
import com.example.food_delivery_app.order.entity.OrderStatus;

import java.time.Instant;

// for lists: NO items, so listing 20 orders does not trigger a query per order (N+1)
public record OrderSummaryResponse(Integer id, Integer restaurantId, OrderStatus status,
                                   Money total, Instant createdAt) {}