package com.example.food_delivery_app.payment.entity;

import com.example.food_delivery_app.payment.gateway.CheckoutSession;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "order_id", nullable = false)
    private Integer orderId;                         // another aggregate: reference by id only

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private BigDecimal amount;
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, length = 3)
    private String currency;
    private String stripeSessionId;
    private String stripePaymentIntentId;
    private String checkoutUrl;
    private Instant createdAt;
    private Instant updatedAt;

    public static Payment pending(Integer orderId, BigDecimal amount, String currency, CheckoutSession session) {
        Payment p = new Payment();
        p.orderId = orderId;
        p.status = PaymentStatus.PENDING;
        p.amount = amount;
        p.currency = currency;
        p.stripeSessionId = session.sessionId();
        p.checkoutUrl = session.url();
        return p;
    }

    public boolean markSucceeded(String paymentIntentId) {
        this.stripePaymentIntentId = paymentIntentId;
        return moveTo(PaymentStatus.SUCCEEDED);
    }
    public boolean markFailed()   { return moveTo(PaymentStatus.FAILED); }
    public boolean markRefunded() { return moveTo(PaymentStatus.REFUNDED); }

    // Returns false (and changes nothing) for an impossible move.
    // Stripe does not guarantee event order, so a late "failed" after "succeeded" must be ignored, not crash.
    private boolean moveTo(PaymentStatus next) {
        if (!status.canMoveTo(next)) return false;
        this.status = next;
        return true;
    }

    @PrePersist void onCreate() { createdAt = updatedAt = Instant.now(); }
    @PreUpdate  void onUpdate() { updatedAt = Instant.now(); }
}