package com.example.food_delivery_app.payment.gateway;

import java.math.BigDecimal;

// What we want to sell, in OUR words. No Stripe type appears here.
public record CheckoutLine(String name, BigDecimal unitPrice, int quantity) {}
