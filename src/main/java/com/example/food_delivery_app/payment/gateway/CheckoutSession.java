package com.example.food_delivery_app.payment.gateway;


// checkout session response from payment gateway
// use to redirect user to payment gateway checkout page
public record CheckoutSession(String sessionId, String url) {}
