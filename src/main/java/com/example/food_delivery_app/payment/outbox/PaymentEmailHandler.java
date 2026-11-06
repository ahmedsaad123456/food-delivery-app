package com.example.food_delivery_app.payment.outbox;

import com.example.food_delivery_app.notification.NotificationMessage;
import com.example.food_delivery_app.notification.notifiction_sender.EmailSender;
import com.example.food_delivery_app.payment.entity.OutboxEvent;
import tools.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEmailHandler implements OutboxEventHandler {

    private final JsonMapper json;
    private final EmailSender emailSender;

    @Override public String eventType() { return OutboxEventTypes.PAYMENT_EMAIL_REQUESTED; }
    @Override public String consumerName() { return "payment-email"; }

    @Override
    public void handle(OutboxEvent event) {
        PaymentSucceededPayload payment = null;
            payment = json.readValue(event.payload(), PaymentSucceededPayload.class);

        emailSender.send(new NotificationMessage(
                payment.userId().toString(),
                "Payment Succeeded",
                "Your payment for order " + payment.orderId() + " has succeeded. Thank you for your purchase!"));
    }
}