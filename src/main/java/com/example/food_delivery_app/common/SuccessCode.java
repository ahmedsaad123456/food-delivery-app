package com.example.food_delivery_app.common;

public enum SuccessCode {
    USER_REGISTERED,
    USER_AUTHENTICATED,

    USER_REFRESH_TOKEN;

    public String messageKey() {
        return "success." + name();
    }
}