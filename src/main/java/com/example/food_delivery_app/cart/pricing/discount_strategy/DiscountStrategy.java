package com.example.food_delivery_app.cart.pricing.discount_strategy;

import com.example.food_delivery_app.common.Money;

import java.math.BigDecimal;

/**
 * Strategy: one way of calculating a discount.
 * CONTRACT (every implementation MUST obey it; DiscountStrategyContractTest enforces it):
 *  - Valid input: subtotal and deliveryFee are non-null and in the same currency, value is non-null and >= 0.
 *  - Never returns null and never throws for valid input.
 *  - Result >= 0.
 *  - Result <= subtotal + deliveryFee (a discount can never make the order cost less than zero).
 *  - Result has the same currency as the inputs.
 * "value" means: the percent for PERCENTAGE, the amount in EGP for FIXED_AMOUNT,
 * and it is ignored by FREE_DELIVERY.
 * Any implementation throwing an exception for valid input is a violation of the contract (LSP).
 */
public interface DiscountStrategy {

    DiscountType type();

    Money calculate(Money subtotal, Money deliveryFee, BigDecimal value);

    // Shared helper: enforces the "<= subtotal + deliveryFee" part of the contract.
    // capped means: if the discount is larger than the total, return the total instead.
    static Money capped(Money discount, Money subtotal, Money deliveryFee) {
        return discount.min(subtotal.add(deliveryFee));
    }
}
