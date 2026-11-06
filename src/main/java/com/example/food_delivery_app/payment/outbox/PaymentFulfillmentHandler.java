package com.example.food_delivery_app.payment.outbox;


import com.example.food_delivery_app.order.entity.order.Order;
import com.example.food_delivery_app.order.repository.OrderRepository;
import com.example.food_delivery_app.order.service.OrderFulfillmentService;
import com.example.food_delivery_app.payment.entity.OutboxEvent;
import tools.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Component
@RequiredArgsConstructor
public class PaymentFulfillmentHandler implements OutboxEventHandler {

    private final JsonMapper json;
    private final OrderFulfillmentService fulfillment; // ETA + driver (async day)
    private final OrderRepository orders;

    @Override public String eventType() { return OutboxEventTypes.PAYMENT_SUCCEEDED; }
    @Override public String consumerName() { return "payment-fulfillment"; }

    @Override
    public void handle(OutboxEvent event) {
        // Jackson 3 throws an unchecked exception on bad JSON, so the processor retries/fails the event
        PaymentSucceededPayload payment = null;
            payment = json.readValue(event.payload(), PaymentSucceededPayload.class);


        // idempotency for work done on OTHER threads (they are not rolled back with our transaction):
        // if a previous attempt already assigned a driver, do not do it again
        boolean alreadyHasDriver = orders.findById(payment.orderId())
                .map(Order::getDriverId)   // empty when driverId is null
                .isPresent();
        if (alreadyHasDriver) return;

        // Wait for the fulfillment task to finish before continuing.
        // This ensures fulfillment completes before the current processing step ends.
        // Wait for fulfillment to finish, but fail if it takes longer than 30 seconds.

        fulfillment.fulfill(payment.orderId())
                .orTimeout(30, TimeUnit.SECONDS)
                .join();    }
}