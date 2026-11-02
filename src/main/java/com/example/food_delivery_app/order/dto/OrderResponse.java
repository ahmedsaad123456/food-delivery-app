package com.example.food_delivery_app.order.dto;

import com.example.food_delivery_app.common.Money;
import com.example.food_delivery_app.order.entity.OrderStatus;

import java.time.Instant;
import java.util.List;
// existing record, now with restaurantId and createdAt
public record OrderResponse(Integer id, Integer restaurantId, OrderStatus status, List<OrderItemResponse> items,
                            Money subtotal, Money deliveryFee, Money discount, Money total,
                            Instant createdAt) {}