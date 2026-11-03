package com.example.food_delivery_app.exceptions;

import com.example.food_delivery_app.common.ErrorCode;

public class PaymentGatewayException extends AppException {
    public PaymentGatewayException(Throwable cause) {
        super(ErrorCode.PAYMENT_PROVIDER_ERROR);
        initCause(cause);
    }
}