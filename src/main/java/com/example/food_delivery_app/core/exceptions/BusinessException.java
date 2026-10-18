package com.example.food_delivery_app.core.exceptions;

import com.example.food_delivery_app.core.exceptions.AppException;
import org.springframework.http.HttpStatus;

/*
* BusinessException is a custom exception class that extends the AppException class.
* It is used to represent business logic errors in the application.
* such as validation errors or other domain-specific errors.
*
* */

public class BusinessException extends AppException {

    public BusinessException(String errorCode, String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, errorCode, message);
    }

    // This constructor is protected to allow subclasses to specify a different HTTP status code if needed.
    protected BusinessException(HttpStatus status, String errorCode, String message) {
        super(status, errorCode, message);
    }
}
