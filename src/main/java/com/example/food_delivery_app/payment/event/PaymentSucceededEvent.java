package com.example.food_delivery_app.payment.event;

public record PaymentSucceededEvent(Integer orderId, Integer userId) {}