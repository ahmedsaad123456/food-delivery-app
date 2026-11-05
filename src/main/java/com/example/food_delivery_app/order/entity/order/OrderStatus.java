package com.example.food_delivery_app.order.entity.order;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum OrderStatus {
    PENDING, PAID, PREPARING, OUT_FOR_DELIVERY, DELIVERED, CANCELLED;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED = Map.of(
            PENDING,          EnumSet.of(PAID, CANCELLED),
            PAID,             EnumSet.of(PREPARING, CANCELLED),
            PREPARING,        EnumSet.of(OUT_FOR_DELIVERY),
            OUT_FOR_DELIVERY, EnumSet.of(DELIVERED),
            DELIVERED,        EnumSet.noneOf(OrderStatus.class),     // final state
            CANCELLED,        EnumSet.noneOf(OrderStatus.class));    // final state

    public boolean canTransitionTo(OrderStatus next) {
        return ALLOWED.get(this).contains(next);
    }
}