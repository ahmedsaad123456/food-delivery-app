package com.example.food_delivery_app.payment.event;

import com.example.food_delivery_app.payment.entity.RefundReason;

import java.math.BigDecimal;

public record RefundRequestedEvent(Integer orderId, Integer userId, BigDecimal amount,
                                   String currency, RefundReason reason) {}