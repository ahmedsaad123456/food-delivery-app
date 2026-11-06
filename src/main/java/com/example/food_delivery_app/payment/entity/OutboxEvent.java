package com.example.food_delivery_app.payment.entity;

public record OutboxEvent(long id, String aggregateId, String type, String payload, int attempts) {}