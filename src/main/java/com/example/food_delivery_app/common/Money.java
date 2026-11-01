package com.example.food_delivery_app.common;


import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * DDD: Domain-Driven Design: Value Object pattern.
 * Domain Driven Design (DDD) is an approach to software development that emphasizes collaboration between technical and domain experts to create a shared understanding of the problem domain.
 * It focuses on modeling complex business logic and rules in a way that reflects the real-world domain.
 * Value Object pattern Definition: a small object that represents a simple entity whose equality is not based on identity, but rather on value.
 * Value Object: an amount of money in a specific currency.
 * Immutable, defined only by its values, and it protects its own rules (invariants):
 *  - amount and currency are never null
 *  - the scale always matches the currency (EGP = 2 decimals), so 10 and 10.00 are equal
 *  - the amount is never negative
 */
public record Money(BigDecimal amount, Currency currency) implements Comparable<Money> {

    public static final Currency EGP = Currency.getInstance(Constants.EGP);

    // Compact constructor: runs for every Money that is ever created.
    public Money {
        Objects.requireNonNull(amount, Constants.AMOUNT);
        Objects.requireNonNull(currency, Constants.CURRENCY);
        amount = amount.setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_UP);
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Money cannot be negative: " + amount + " " + currency);
        }
    }

    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    public static Money egpMoney(BigDecimal amount) {
        return new Money(amount, EGP);
    }



    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    // Every operation returns a NEW Money. The original is never changed.
    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    // Throws IllegalArgumentException if the result would be negative.
    public Money subtract(Money other) {
        requireSameCurrency(other);
        return new Money(amount.subtract(other.amount), currency);
    }

    // price x quantity
    public Money multiply(int quantity) {
        return new Money(amount.multiply(BigDecimal.valueOf(quantity)), currency);
    }

    // price x factor, for example 0.14 for 14% tax
    public Money multiply(BigDecimal factor) {
        return new Money(amount.multiply(factor), currency);
    }

    public Money min(Money other) {
        requireSameCurrency(other);
        return compareTo(other) <= 0 ? this : other;
    }

    @Override
    public int compareTo(@NotNull Money other) {
        requireSameCurrency(other);
        return amount.compareTo(other.amount);
    }

    private void requireSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Currency mismatch: " + currency + " vs " + other.currency);
        }
    }
}
