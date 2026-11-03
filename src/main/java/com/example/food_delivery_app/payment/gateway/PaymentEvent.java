package com.example.food_delivery_app.payment.gateway;

// Our own description of "something happened with a payment"
public record PaymentEvent(
        String eventId,
        PaymentEventType type,
        String sessionId,          // only for CHECKOUT_COMPLETED
        String paymentReference,   // Stripe payment intent id
        Integer orderId) {}        // from our metadata, may be null