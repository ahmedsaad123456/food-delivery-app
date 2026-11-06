package com.example.food_delivery_app.payment.outbox;


import com.example.food_delivery_app.payment.entity.OutboxEvent;

public interface OutboxEventHandler {
    String eventType();      // which event type this handler wants, e.g. PAYMENT_SUCCEEDED
    String consumerName();   // unique and STABLE name, used for idempotency
    void handle(OutboxEvent event); // throw any RuntimeException to make the processor retry
}