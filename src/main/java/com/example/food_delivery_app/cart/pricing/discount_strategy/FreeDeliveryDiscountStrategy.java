package com.example.food_delivery_app.cart.pricing.discount_strategy;

import com.example.food_delivery_app.common.Money;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FreeDeliveryDiscountStrategy implements DiscountStrategy {

    @Override
    public DiscountType type() {
        return DiscountType.FREE_DELIVERY;
    }

    // "value" is ignored: the discount is the delivery fee itself (always <= subtotal + deliveryFee).
    @Override
    public Money calculateDiscount(Money subtotal, Money deliveryFee, BigDecimal value) {
        return deliveryFee;
    }
}
