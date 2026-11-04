package com.example.food_delivery_app.payment.service;

import com.example.food_delivery_app.payment.entity.Payment;
import com.example.food_delivery_app.payment.entity.RefundReason;
import com.example.food_delivery_app.payment.gateway.PaymentRefund;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefundService {

    private final PaymentRefund refund;          // ISP: only the refund port

    // Full refund of one payment. Throws PaymentGatewayException if Stripe fails.
    public void refundFully(Payment payment, RefundReason reason) {
        if (payment.getStripePaymentIntentId() == null) {
            throw new IllegalStateException("Payment " + payment.getId() + " has no payment intent to refund");
        }
        // ONE key per payment: if this runs twice (a retry), Stripe returns the SAME refund and never a second one
        String idempotencyKey = "refund-payment-" + payment.getId();
        refund.refundPayment(payment.getStripePaymentIntentId(), payment.getAmount(),
                payment.getCurrency(), idempotencyKey);
        log.warn("Refund requested for payment {} (order {}): {}", payment.getId(), payment.getOrderId(), reason);
    }
}