package com.example.food_delivery_app.cart.pricing.price_decorator;

import com.example.food_delivery_app.common.Money;


// Decorator pattern: each decorator adds a fee to the subtotal, and passes the result to the next decorator.
public interface PriceCalculator {

    // Takes the items subtotal, returns the amount with all fees added.
    Money calculatePrice(Money subtotal);
}
