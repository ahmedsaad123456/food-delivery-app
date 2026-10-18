package com.example.food_delivery_app.core.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/*
 * Base class for all application exceptions.
 * Each exception has an associated HTTP status code and an error code.
 * The error code is a string that uniquely identifies the type of error.
 * */


@Getter
public abstract class AppException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    protected AppException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }
}
