package com.example.food_delivery_app.payment.repoistory;

import com.example.food_delivery_app.payment.entity.Payment;
import com.example.food_delivery_app.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    long countByOrderId(Integer orderId);

    Optional<Payment> findByStripeSessionId(String sessionId);

    Optional<Payment> findByStripePaymentIntentId(String paymentIntentId);

    // newest PENDING attempt that is still young enough to be open on Stripe
    // find first payment by order id and status and created after specific time
    Optional<Payment> findFirstByOrderIdAndStatusAndCreatedAtAfterOrderByCreatedAtDesc(
            Integer orderId, PaymentStatus status, Instant after);

    // used by payment_intent.payment_failed (we only know the order id from metadata)
    // find first payment by order id and group of status and created after specific time
    Optional<Payment> findFirstByOrderIdAndStatusInOrderByCreatedAtDesc(
            Integer orderId, Collection<PaymentStatus> statuses);
}