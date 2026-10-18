package com.example.food_delivery_app.core.exceptions;

import com.example.food_delivery_app.core.exceptions.AppException;
import org.springframework.http.HttpStatus;

/*
* UnauthorizedException is a custom exception class that extends the AppException class.
* It is used to represent a specific type of error that occurs when a user is not authorized to access a resource or perform an action.
* */
public class UnauthorizedException extends AppException {

    public UnauthorizedException(String errorCode, String message) {
        super(HttpStatus.UNAUTHORIZED, errorCode, message);
    }
}
