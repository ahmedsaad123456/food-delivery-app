package com.example.food_delivery_app.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // generic
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
    FORBIDDEN(HttpStatus.FORBIDDEN),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND),
    DATA_CONFLICT(HttpStatus.CONFLICT),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS),
    // auth
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED),
    EMAIL_ALREADY_REGISTERED(HttpStatus.CONFLICT),
    TEMP_SESSION_EXPIRED(HttpStatus.UNAUTHORIZED),

    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED);


    private final HttpStatus status;

    public String messageKey() {            // the key used inside messages.properties
        return "error." + name();
    }
}