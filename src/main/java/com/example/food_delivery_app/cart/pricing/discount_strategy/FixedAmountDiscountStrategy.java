package com.example.food_delivery_app.cart.pricing.discount_strategy;

import com.example.food_delivery_app.common.Money;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FixedAmountDiscountStrategy implements DiscountStrategy {

    @Override
    public DiscountType type() {
        return DiscountType.FIXED_AMOUNT;
    }

    @Override
    public Money calculate(Money subtotal, Money deliveryFee, BigDecimal amount) {
        Money fixed = Money.of(amount, subtotal.currency());
        // return the smallest of the fixed discount and the total (subtotal + deliveryFee) to ensure we don't exceed the total cost
        return DiscountStrategy.capped(fixed, subtotal, deliveryFee);
    }
}
