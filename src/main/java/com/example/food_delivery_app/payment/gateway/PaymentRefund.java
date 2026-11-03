package com.example.food_delivery_app.payment.gateway;

import java.math.BigDecimal;

// Port 2: used ONLY by the future refund/cancel flow
public interface PaymentRefund {
    void refund(String paymentReference, BigDecimal amount, String currency, String idempotencyKey);
}