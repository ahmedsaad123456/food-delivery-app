package com.example.food_delivery_app.order.entity;

import com.example.food_delivery_app.common.Money;

// input of Order.create: a plain value, already a snapshot
public record OrderLine(Integer menuItemId, String name, Money unitPrice, int quantity) {
    public Money lineTotal() { return unitPrice.multiply(quantity); }
}