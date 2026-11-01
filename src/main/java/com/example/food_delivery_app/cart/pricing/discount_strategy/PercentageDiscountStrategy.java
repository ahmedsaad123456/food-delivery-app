package com.example.food_delivery_app.cart.pricing.discount_strategy;

import com.example.food_delivery_app.common.Money;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PercentageDiscountStrategy implements DiscountStrategy {

    @Override
    public DiscountType type() {
        return DiscountType.PERCENTAGE;
    }

    @Override
    public Money calculate(Money subtotal, Money deliveryFee, BigDecimal percent) {
        BigDecimal factor = percent.movePointLeft(2);              // 10 -> 0.10
        // percent > 100 is still "valid input", so we cap instead of throwing (LSP)
        return DiscountStrategy.capped(subtotal.multiply(factor), subtotal, deliveryFee);
    }
}
