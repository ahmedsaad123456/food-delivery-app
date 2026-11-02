package com.example.food_delivery_app.order.events;

// event to notify other services that an order has been placed
public record OrderPlacedEvent(Integer orderId, Integer userId) {}