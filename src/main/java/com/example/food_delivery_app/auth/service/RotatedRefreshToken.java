package com.example.food_delivery_app.auth.service;

public record RotatedRefreshToken (
        int userId, String newToken
) {
}