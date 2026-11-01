package com.example.food_delivery_app.cart.pricing.price_decorator;

import com.example.food_delivery_app.common.Money;

import java.math.BigDecimal;

public class ServiceFeeDecorator extends PriceCalculatorDecorator {

    private final BigDecimal rate;

    public ServiceFeeDecorator(PriceCalculator inner, BigDecimal rate) {
        super(inner);
        this.rate = rate;
    }

    @Override
    protected Money extraCharge(Money subtotal) {
        return subtotal.multiply(rate);
    }
}
