package com.example.food_delivery_app.cart.pricing.discount_strategy;

import com.example.food_delivery_app.common.Money;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


// discount service is a strategy pattern implementation that uses Spring's dependency injection to manage different discount strategies.
// Each strategy implements the DiscountStrategy interface, which defines how to calculate discounts based on the type of discount (percentage, fixed amount, or free delivery).
// The DiscountService class collects all available strategies and ensures that there is a registered strategy for each discount type.
// It provides a method to calculate the discount based on the specified type, subtotal, delivery fee, and value.
@Service
public class DiscountService {

    private final Map<DiscountType, DiscountStrategy> strategies;

    // Spring injects EVERY bean that implements DiscountStrategy into this list.
    public DiscountService(List<DiscountStrategy> all) {
        // toMap throws IllegalStateException if two strategies share a type -> app fails at startup
        this.strategies = all.stream()
                .collect(Collectors.toMap(DiscountStrategy::type, Function.identity()));
        for (DiscountType type : DiscountType.values()) {
            if (!strategies.containsKey(type)) {
                throw new IllegalStateException("No DiscountStrategy registered for " + type);
            }
        }
    }

    public Money discountFor(DiscountType type, Money subtotal, Money deliveryFee, BigDecimal value) {
        return strategies.get(type).calculateDiscount(subtotal, deliveryFee, value);
    }
}
