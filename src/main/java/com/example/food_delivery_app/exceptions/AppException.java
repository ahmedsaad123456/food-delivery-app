package com.example.food_delivery_app.exceptions;

import com.example.food_delivery_app.common.ErrorCode;
import lombok.Getter;

import java.util.Arrays;

/*
 * Base class for all application exceptions.
 * Each exception has an associated HTTP status code and an error code.
 * The error code is a string that uniquely identifies the type of error.
 * */
@Getter
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String[] args;            // values for {0}, {1} in the message (as text)

    public AppException(ErrorCode errorCode, Object... args) {
        super(errorCode.name());            // shows the code in logs
        this.errorCode = errorCode;
        // Convert to String right away: Strings are serializable, and we don't keep
        // references to big objects (like entities) inside an exception.
        this.args = Arrays.stream(args).map(String::valueOf).toArray(String[]::new);
    }
}
