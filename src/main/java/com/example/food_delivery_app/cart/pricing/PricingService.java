package com.example.food_delivery_app.cart.pricing;

import com.example.food_delivery_app.cart.pricing.discount_strategy.DiscountService;
import com.example.food_delivery_app.cart.pricing.discount_strategy.DiscountType;
import com.example.food_delivery_app.cart.pricing.price_decorator.PriceCalculator;
import com.example.food_delivery_app.common.ErrorCode;
import com.example.food_delivery_app.common.Money;
import com.example.food_delivery_app.exceptions.AppException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PricingService {

    private final PriceCalculator calculator;
    private final DiscountService discountService;
    private final Money deliveryFee;

    public PricingService(PriceCalculator calculator,
                          DiscountService discountService,
                          @Value("${app.pricing.delivery-fee:30.00}") BigDecimal deliveryFee) {
        this.calculator = calculator;
        this.discountService = discountService;
        this.deliveryFee = Money.egpMoney(deliveryFee);
    }

    // total = (subtotal + tax + service fee) + delivery fee - discount
    public PriceSummary totalPriceWithoutDiscount(Money subtotal, DiscountType type, BigDecimal discountValue) {
        Money discount = Money.zero(subtotal.currency());
        if (type != null) {
            BigDecimal value = discountValue == null ? BigDecimal.ZERO : discountValue;
            if (value.signum() < 0) {
                throw new AppException(ErrorCode.VALIDATION_FAILED);
            }
            discount = discountService.discountFor(type, subtotal, deliveryFee, value);
        }
        // The discount contract (<= subtotal + deliveryFee) guarantees this never goes negative.
        Money total = calculator.calculatePrice(subtotal).add(deliveryFee).subtract(discount);
        return new PriceSummary(subtotal, deliveryFee, discount, total);
    }
}
