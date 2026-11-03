package com.example.food_delivery_app.payment.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;


// structure for Payment properties, including currency, success URL, cancel URL, and reuse window
@ConfigurationProperties(prefix = "app.payment") // specifies that the properties will be prefixed with "app.payment" in the application.yml file
public record PaymentProperties(String currency, String successUrl, String cancelUrl, Duration reuseWindow, Duration SessionLeftTime) {}