package com.example.food_delivery_app.cart.pricing.price_decorator;

import com.example.food_delivery_app.cart.pricing.price_decorator.BasePriceCalculator;
import com.example.food_delivery_app.cart.pricing.price_decorator.PriceCalculator;
import com.example.food_delivery_app.cart.pricing.price_decorator.ServiceFeeDecorator;
import com.example.food_delivery_app.cart.pricing.price_decorator.TaxDecorator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

//+-----------------------------------------+
//        | ServiceFeeDecorator  (+12% of subtotal) |
//        |  +-----------------------------------+  |
//        |  | TaxDecorator      (+14% of subtotal) |
//        |  |  +-----------------------------+  |  |
//        |  |  | BasePriceCalculator         |  |  |
//        |  |  | (returns the subtotal)      |  |  |
//        |  |  +-----------------------------+  |  |
//        |  +-----------------------------------+  |
//        +-----------------------------------------+
@Configuration
public class PricingConfig {

    // The ONE place that decides which decorators wrap the base calculator, and in what order.
    // Remove a line here to remove a fee. No other class changes.
    @Bean
    public PriceCalculator priceCalculator(
            @Value("${app.pricing.tax-rate:0.14}") BigDecimal taxRate,
            @Value("${app.pricing.service-fee-rate:0.12}") BigDecimal serviceFeeRate) {
        PriceCalculator calculator = new BasePriceCalculator();
        calculator = new TaxDecorator(calculator, taxRate);
        calculator = new ServiceFeeDecorator(calculator, serviceFeeRate);
        return calculator;
    }
}
