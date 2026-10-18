package com.example.food_delivery_app.features.auth.auth_exceptions;


import com.example.food_delivery_app.core.exceptions.ConflictException;

public class EmailAlreadyRegisteredException extends ConflictException {
    public EmailAlreadyRegisteredException(String email) {
        super("EMAIL_ALREADY_REGISTERED", "Email already registered: " + email);
    }
}
