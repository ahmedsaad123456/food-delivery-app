package com.example.food_delivery_app.order.controller;

import com.example.food_delivery_app.common.ApiResponse;
import com.example.food_delivery_app.common.PageResponse;
import com.example.food_delivery_app.order.dto.OrderResponse;
import com.example.food_delivery_app.order.dto.OrderSummaryResponse;
import com.example.food_delivery_app.order.dto.PlaceOrderRequest;
import com.example.food_delivery_app.order.entity.OrderStatus;
import com.example.food_delivery_app.order.service.OrderService;
import com.example.food_delivery_app.users.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ORDER_CREATE')")
    public ApiResponse<OrderResponse> placeOrder(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody(required = false) PlaceOrderRequest request) {
        PlaceOrderRequest body = request != null ? request : new PlaceOrderRequest(null, null);
        OrderResponse order = orderService.placeOrder(user.getId(), body);
        return ApiResponse.ok(order);
    }

    // ---------- reads ----------

    @GetMapping("/orders/me")
    @PreAuthorize("hasAuthority('ORDER_VIEW_OWN')")
    public ApiResponse<PageResponse<OrderSummaryResponse>> getMyOrders(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) OrderStatus status,
            @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(orderService.getMyOrders(user.getId(), status, pageable));
    }

    @GetMapping("/orders/{id}")
    @PreAuthorize("@orderSecurity.canView(#id, authentication)")
    public ApiResponse<OrderResponse> getOrder(@PathVariable Integer id) {
        return ApiResponse.ok(orderService.getOrder(id));
    }

    @GetMapping("/restaurants/{restaurantId}/orders")
    @PreAuthorize("hasRole('ADMIN') or (hasAuthority('ORDER_VIEW_ALL') "
            + "and @restaurantSecurity.isOwner(#restaurantId, authentication))")
    public ApiResponse<PageResponse<OrderSummaryResponse>> restaurantOrders(
            @PathVariable Integer restaurantId,
            @RequestParam(required = false) OrderStatus status,
            @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(orderService.getRestaurantOrders(restaurantId, status, pageable));
    }

    // ---------- state changes ----------

    @PatchMapping("/orders/{id}/cancel")
    @PreAuthorize("hasAuthority('ORDER_CANCEL') and @orderSecurity.isCustomerOf(#id, authentication)")
    public ApiResponse<OrderResponse> cancelOrder(@PathVariable Integer id) {
        return ApiResponse.ok(orderService.cancel(id));
    }

    @PatchMapping("/orders/{id}/start-preparing")
    @PreAuthorize("hasAuthority('ORDER_UPDATE_STATUS') and @orderSecurity.isRestaurantOwnerOf(#id, authentication)")
    public ApiResponse<OrderResponse> startPreparing(@PathVariable Integer id) {
        return ApiResponse.ok(orderService.startPreparing(id));
    }

    @PatchMapping("/orders/{id}/out-for-delivery")
    @PreAuthorize("hasAuthority('ORDER_UPDATE_STATUS') and @orderSecurity.isRestaurantOwnerOf(#id, authentication)")
    public ApiResponse<OrderResponse> outForDelivery(@PathVariable Integer id) {
        return ApiResponse.ok(orderService.markOutForDelivery(id));
    }

    @PatchMapping("/orders/{id}/delivered")
    @PreAuthorize("hasAuthority('DELIVERY_UPDATE')")        // day 14: only the driver assigned to this order
    public ApiResponse<OrderResponse> delivered(@PathVariable Integer id) {
        return ApiResponse.ok(orderService.markDelivered(id));
    }
}
