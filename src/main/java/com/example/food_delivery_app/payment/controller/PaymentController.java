package com.example.food_delivery_app.payment.controller;

import com.example.food_delivery_app.common.ApiResponse;
import com.example.food_delivery_app.payment.dto.CheckoutResponse;
import com.example.food_delivery_app.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // This endpoint is for creating a checkout session for an order.
    // It requires the user to have the 'ORDER_CREATE' authority and to be the owner of the order.
    // The orderId is passed as a path variable, and the method returns a CheckoutResponse wrapped in an ApiResponse.
    @PostMapping("/orders/{orderId}/checkout")
    @PreAuthorize("hasAuthority('ORDER_CREATE') and @orderSecurity.isCustomerOf(#orderId, authentication)")
    public ApiResponse<CheckoutResponse> checkout(@PathVariable Integer orderId) {
        return ApiResponse.ok( paymentService.createCheckout(orderId));
    }
}