package com.example.food_delivery_app.payment.outbox;


public final class OutboxEventTypes {
    private OutboxEventTypes() {}
    public static final String PAYMENT_SUCCEEDED = "PaymentSucceeded";

    public static final String PAYMENT_EMAIL_REQUESTED = "PaymentEmailRequested";

}
