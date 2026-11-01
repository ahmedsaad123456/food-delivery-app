package com.example.food_delivery_app.cart.pricing;

import com.example.food_delivery_app.common.Money;

public record PriceSummary(Money subtotal, Money deliveryFee, Money discount, Money total) {}
