package com.example.food_delivery_app.payment.entity;

public enum PaymentStatus {
    PENDING, SUCCEEDED, FAILED, EXPIRED, REFUNDED;

    // FAILED -> SUCCEEDED is allowed: the customer can retry with another card on the same page
    public boolean canMoveTo(PaymentStatus next) {
        return switch (this) {
            case PENDING   -> next == SUCCEEDED || next == FAILED || next == EXPIRED;
            case FAILED    -> next == SUCCEEDED || next == EXPIRED;
            case SUCCEEDED -> next == REFUNDED;
            case EXPIRED, REFUNDED -> false;
        };
    }
}