package com.example.food_delivery_app.payment.gateway;

// Port 1: used ONLY by PaymentService
public interface CheckoutSessionCreator {
    CheckoutSession create(CheckoutRequest request);
}
