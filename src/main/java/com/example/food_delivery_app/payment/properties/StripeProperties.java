package com.example.food_delivery_app.payment.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;


// structure for Stripe properties, including secret key and webhook secret
// use it in the StripeService class to access the properties
@Validated
@ConfigurationProperties(prefix = "stripe") // specifies that the properties will be prefixed with "stripe" in the application.yml file
public record StripeProperties(@NotBlank String secretKey, String webhookSecret) {}
