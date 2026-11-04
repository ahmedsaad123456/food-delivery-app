package com.example.food_delivery_app.payment.entity;

public enum RefundReason {
    OUT_OF_STOCK,        // paid, but an item ran out before we could take it
    ORDER_NOT_PAYABLE    // paid, but the order is already PAID (double payment) or CANCELLED
}