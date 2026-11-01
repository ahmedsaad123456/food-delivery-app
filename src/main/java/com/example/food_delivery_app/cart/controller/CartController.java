package com.example.food_delivery_app.cart.controller;


import com.example.food_delivery_app.cart.dto.AddToCartRequest;
import com.example.food_delivery_app.cart.dto.CartResponse;
import com.example.food_delivery_app.cart.dto.UpdateQuantityRequest;
import com.example.food_delivery_app.cart.pricing.discount_strategy.DiscountType;
import com.example.food_delivery_app.cart.pricing.PriceSummary;
import com.example.food_delivery_app.cart.service.CartService;
import com.example.food_delivery_app.common.ApiResponse;
import com.example.food_delivery_app.users.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

// The userId always comes from the token (principal), never from the URL -> nobody can read another user's cart.
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ApiResponse<CartResponse> getCart(@AuthenticationPrincipal User user) {
        return ApiResponse.ok(cartService.getCart(user.getId()));
    }

    @GetMapping("/summary")
    public ApiResponse<PriceSummary> summary(@AuthenticationPrincipal User user,
                                             @RequestParam(required = false) DiscountType discountType,
                                             @RequestParam(required = false) BigDecimal discountValue) {
        return ApiResponse.ok(cartService.summary(user.getId(), discountType, discountValue));
    }

    @PostMapping("/items")
    public ResponseEntity<Void> add(@AuthenticationPrincipal User user,
                                    @Valid @RequestBody AddToCartRequest request) {
        cartService.addToCart(user.getId(), request.menuItemId(), request.quantity());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/items/{menuItemId}")
    public ResponseEntity<Void> update(@AuthenticationPrincipal User user,
                                       @PathVariable Integer menuItemId,
                                       @Valid @RequestBody UpdateQuantityRequest request) {
        cartService.updateQuantity(user.getId(), menuItemId, request.quantity());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/items/{menuItemId}")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal User user, @PathVariable Integer menuItemId) {
        cartService.removeFromCart(user.getId(), menuItemId);
        return ResponseEntity.noContent().build();
    }
}
