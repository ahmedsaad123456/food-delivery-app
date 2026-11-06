package com.example.food_delivery_app.payment.outbox;


public record PaymentSucceededPayload(Integer orderId, Integer userId) {}