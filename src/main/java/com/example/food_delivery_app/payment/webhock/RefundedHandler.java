package com.example.food_delivery_app.payment.webhock;

import com.example.food_delivery_app.payment.gateway.PaymentEvent;
import com.example.food_delivery_app.payment.gateway.PaymentEventType;
import com.example.food_delivery_app.payment.repoistory.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefundedHandler implements WebhookHandler {

    private final PaymentRepository payments;

    @Override public PaymentEventType type() { return PaymentEventType.REFUNDED; }

    @Override
    public void handleEvent(PaymentEvent event) {
        payments.findByStripePaymentIntentId(event.paymentReference())
                .ifPresentOrElse(p -> {
                    // The payment is already refunded in our DB, or it is not refundable (e.g. PENDING). In both cases we ignore the event.
                    if (!p.markRefunded()) log.info("Payment {} cannot be refunded from {}: ignored", p.getId(), p.getStatus());
                }, () -> log.warn("Refund for unknown payment intent {}: ignored", event.paymentReference()));
    }
}