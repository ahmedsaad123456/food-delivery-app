package com.example.food_delivery_app.order.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// Runs immediately, in the SAME thread and the SAME transaction as placeOrder
@Component
@Slf4j
public class OrderPlacedAuditListener {

    // logs the order placed event for auditing purposes
    // event listener method that listens for OrderPlacedEvent and logs the order details
    @EventListener
    public void on(OrderPlacedEvent event) {
        log.info("Order {} placed by user {}", event.orderId(), event.userId());
    }
}