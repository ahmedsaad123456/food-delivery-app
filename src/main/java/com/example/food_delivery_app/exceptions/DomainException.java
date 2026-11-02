package com.example.food_delivery_app.exceptions;

import com.example.food_delivery_app.common.ErrorCode;

public class DomainException extends AppException {
    public DomainException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }
}