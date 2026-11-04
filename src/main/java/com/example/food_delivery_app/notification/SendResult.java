package com.example.food_delivery_app.notification;

// parts = how many provider calls were needed (an SMS split into 3 segments = 3 parts)
// sendResult is a Value Object: it is immutable and has no behavior, only data.
// use it to return the result of sending a notification to the caller.
public record SendResult(NotificationChannel channel, int parts, String message) {}