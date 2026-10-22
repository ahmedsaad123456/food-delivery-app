package com.example.food_delivery_app.common;

public enum SuccessCode {
    USER_REGISTERED,
    USER_AUTHENTICATED;

    public String messageKey() {
        return "success." + name();
    }
}