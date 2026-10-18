package com.example.food_delivery_app.features.auth.auth_exceptions;


import com.example.food_delivery_app.core.exceptions.UnauthorizedException;

public class TempSessionExpiredException extends UnauthorizedException {
    public TempSessionExpiredException() {
        super("TEMP_SESSION_EXPIRED", "Temporary session expired. Please verify OTP again.");
    }
}
