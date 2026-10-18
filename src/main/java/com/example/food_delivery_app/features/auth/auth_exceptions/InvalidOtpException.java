package com.example.food_delivery_app.features.auth.auth_exceptions;


import com.example.food_delivery_app.core.exceptions.BusinessException;

public class InvalidOtpException extends BusinessException {
    public InvalidOtpException() {
        super("INVALID_OTP", "Invalid OTP");
    }
}
