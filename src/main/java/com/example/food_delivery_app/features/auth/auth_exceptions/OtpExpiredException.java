package com.example.food_delivery_app.features.auth.auth_exceptions;


import com.example.food_delivery_app.core.exceptions.BusinessException;

public class OtpExpiredException extends BusinessException {
    public OtpExpiredException() {
        super("OTP_EXPIRED", "OTP expired");
    }
}
