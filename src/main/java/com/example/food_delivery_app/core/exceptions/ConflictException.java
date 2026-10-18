package com.example.food_delivery_app.core.exceptions;

import com.example.food_delivery_app.core.exceptions.AppException;
import org.springframework.http.HttpStatus;

/*
* ConflictException is a custom exception class that extends the AppException class.
* It is used to represent a specific type of error that occurs when there is a conflict in the application, such as when trying to create a resource that already exists.
*
* */
public class ConflictException extends AppException {

    public ConflictException(String errorCode, String message) {
        super(HttpStatus.CONFLICT, errorCode, message);
    }
}
