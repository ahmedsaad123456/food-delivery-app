package com.example.food_delivery_app.cart.pricing.price_decorator;

import com.example.food_delivery_app.common.Money;

// The core: no fees, just the subtotal. NOT a @Component on purpose (see PricingConfig).
public class BasePriceCalculator implements PriceCalculator {

    @Override
    public Money calculate(Money subtotal) {
        return subtotal;
    }
}
