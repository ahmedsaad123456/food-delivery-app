package com.example.food_delivery_app.cart.pricing.price_decorator;

import com.example.food_delivery_app.common.Money;

/**
 * Decorator base: it IS a PriceCalculator, and it HAS a PriceCalculator (inner).
 * calculate() = whatever the inner one returns + this decorator's own extra charge.
 */
public abstract class PriceCalculatorDecorator implements PriceCalculator {

    private final PriceCalculator inner;

    protected PriceCalculatorDecorator(PriceCalculator inner) {
        this.inner = inner;
    }

    @Override
    public final Money calculatePrice(Money subtotal) {
        return inner.calculatePrice(subtotal).add(extraCharge(subtotal));
    }

    // Computed from the ORIGINAL subtotal, so fees never compound on each other.
    protected abstract Money extraCharge(Money subtotal);
}
