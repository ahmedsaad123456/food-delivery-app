package com.example.food_delivery_app.payment.controller;

import com.example.food_delivery_app.common.ApiResponse;
import com.example.food_delivery_app.common.Constants;
import com.example.food_delivery_app.payment.gateway.PaymentEvent;
import com.example.food_delivery_app.payment.gateway.WebhookSignatureVerifier;
import com.example.food_delivery_app.payment.service.WebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payment")
public class StripeWebhookController {

    private final WebhookSignatureVerifier verifier;     // ISP: this controller sees only the verifier port
    private final WebhookService webhookService;

    @PostMapping("/webhook")
    public ApiResponse<Void> handle(@RequestBody byte[] payload,                    // RAW bytes, untouched
                                    @RequestHeader("Stripe-Signature") String signature) {
        // The signature is computed over the exact text Stripe sent, so we decode the bytes as UTF-8 ourselves
        PaymentEvent event = verifier.verify(new String(payload, StandardCharsets.UTF_8), signature);
        webhookService.process(event);
        return ApiResponse.ok(Constants.WEBHOOK_SUCCESS, null);
    }


}