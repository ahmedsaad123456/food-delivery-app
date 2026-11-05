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
    MENU_ITEM_UNAVAILABLE(HttpStatus.BAD_REQUEST),
    DATA_CONFLICT(HttpStatus.CONFLICT),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS),
    INVALID_SORT_FIELD(HttpStatus.BAD_REQUEST),
    CART_MIXED_RESTAURANTS(HttpStatus.CONFLICT),
    INSUFFICIENT_STOCK(HttpStatus.CONFLICT),
    PHONE_NUMBER_ALREADY_REGISTERED(HttpStatus.CONFLICT),
    DRIVER_IS_BUSY(HttpStatus.BAD_REQUEST),
    USER_REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT),
    ORDER_HAS_ALREADY_DRIVER(HttpStatus.CONFLICT),
    NO_DRIVER_AVAILABLE(HttpStatus.NOT_FOUND),

    // ErrorCode
    ORDER_INVALID_TRANSITION(HttpStatus.CONFLICT),
    ORDER_EMPTY(HttpStatus.UNPROCESSABLE_CONTENT),
    // auth
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED),
    EMAIL_ALREADY_REGISTERED(HttpStatus.CONFLICT),
    TEMP_SESSION_EXPIRED(HttpStatus.UNAUTHORIZED),

    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED),

    // payment
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND),
    PAYMENT_ORDER_NOT_PAYABLE(HttpStatus.BAD_REQUEST),
    PAYMENT_PROVIDER_ERROR(HttpStatus.BAD_GATEWAY);






    private final HttpStatus status;

    public String messageKey() {            // the key used inside messages.properties
        return "error." + name();
    }
}