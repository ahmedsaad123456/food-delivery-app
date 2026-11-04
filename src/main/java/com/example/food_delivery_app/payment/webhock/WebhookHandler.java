package com.example.food_delivery_app.payment.webhock;

import com.example.food_delivery_app.payment.gateway.PaymentEvent;
import com.example.food_delivery_app.payment.gateway.PaymentEventType;

// One handler per event type. Spring finds all of them (like DiscountStrategy).
public interface WebhookHandler {
    PaymentEventType type();
    void handleEvent(PaymentEvent event);
}