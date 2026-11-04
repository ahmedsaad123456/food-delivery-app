package com.example.food_delivery_app.payment.gateway;


import java.math.BigDecimal;
import java.util.List;

public record CheckoutRequest(
        Integer orderId,
        String currency,               // "EGP"
        List<CheckoutLine> lines,
        BigDecimal discount,           // 0 when there is no discount
        String successUrl,
        String cancelUrl,
        String idempotencyKey  // to prevent double charging if the user refreshes the page or clicks back and forth between the payment page and the success page


) {}
