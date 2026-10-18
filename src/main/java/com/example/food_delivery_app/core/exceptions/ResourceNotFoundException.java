package com.example.food_delivery_app.core.exceptions;

import com.example.food_delivery_app.core.exceptions.AppException;
import org.springframework.http.HttpStatus;

/*
* ResourceNotFoundException is a custom exception class that extends the AppException class.
* It is used to represent a specific type of error that occurs when a requested resource is not found in the application.
*
* */
public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException(String resource, Object id) {
        super(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "%s not found with id: %s".formatted(resource, id));
    }

    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", message);
    }
}
