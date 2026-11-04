package com.example.food_delivery_app.payment.gateway;

// Port 3: used ONLY by the webhook controller/service
public interface WebhookSignatureVerifier {
    // throws InvalidWebhookSignatureException when the signature is wrong
    PaymentEvent verifyStripeSignature(String payload, String signatureHeader);
}