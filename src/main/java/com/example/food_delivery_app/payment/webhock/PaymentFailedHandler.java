package com.example.food_delivery_app.payment.webhock;

import com.example.food_delivery_app.payment.entity.Payment;
import com.example.food_delivery_app.payment.entity.PaymentStatus;
import com.example.food_delivery_app.payment.gateway.PaymentEvent;
import com.example.food_delivery_app.payment.gateway.PaymentEventType;
import com.example.food_delivery_app.payment.repoistory.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentFailedHandler implements WebhookHandler {

    private final PaymentRepository payments;

    @Override public PaymentEventType type() { return PaymentEventType.PAYMENT_FAILED; }

    @Override
    public void handle(PaymentEvent event) {
        if (event.orderId() == null) {
            log.warn("payment_failed without orderId metadata: ignored");
            return;
        }
        // The ORDER stays PENDING: the customer may retry with another card on the same Stripe page.
        payments.findFirstByOrderIdAndStatusInOrderByCreatedAtDesc(event.orderId(), List.of(PaymentStatus.PENDING))
                .ifPresentOrElse(Payment::markFailed,
                        () -> log.info("No PENDING payment for order {}: ignored", event.orderId()));
    }
}