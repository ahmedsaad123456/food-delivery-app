package com.example.food_delivery_app.order.controller;

import com.example.food_delivery_app.common.ApiResponse;
import com.example.food_delivery_app.common.PageQuery;
import com.example.food_delivery_app.common.PageResponse;
import com.example.food_delivery_app.order.dto.driver.AssignDriverRequest;
import com.example.food_delivery_app.order.dto.driver.DriverResponse;
import com.example.food_delivery_app.order.dto.order.OrderResponse;
import com.example.food_delivery_app.order.dto.order.OrderSummaryResponse;
import com.example.food_delivery_app.order.dto.order.PlaceOrderRequest;
import com.example.food_delivery_app.order.dto.order.TimelineEntryResponse;
import com.example.food_delivery_app.order.entity.order.OrderStatus;
import com.example.food_delivery_app.order.service.DriverAssignmentService;
import com.example.food_delivery_app.order.service.OrderService;
import com.example.food_delivery_app.users.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final DriverAssignmentService assignmentService;

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

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ORDER_VIEW_OWN')")
    public ApiResponse<PageResponse<OrderSummaryResponse>> getMyOrders(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) OrderStatus status,@Valid @ParameterObject PageQuery pageQuery
            ) {
        return ApiResponse.ok(orderService.getMyOrders(user.getId(), status, pageQuery));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@orderSecurity.canView(#id, authentication)")
    public ApiResponse<OrderResponse> getOrderById(@PathVariable Integer id) {
        return ApiResponse.ok(orderService.getOrderById(id));
    }

    @GetMapping("/restaurants/{restaurantId}")
    @PreAuthorize("hasRole('ADMIN') or (hasAuthority('ORDER_VIEW_ALL') "
            + "and @restaurantSecurity.isOwner(#restaurantId, authentication))")
    public ApiResponse<PageResponse<OrderSummaryResponse>> restaurantOrders(
            @PathVariable Integer restaurantId,
            @RequestParam(required = false) OrderStatus status,
            @Valid @ParameterObject PageQuery pageQuery) {
        return ApiResponse.ok(orderService.getRestaurantOrders(restaurantId, status, pageQuery));
    }

    // ---------- state changes ----------

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('ORDER_CANCEL') and @orderSecurity.isCustomerOf(#id, authentication)")
    public ApiResponse<OrderResponse> cancelOrder(@PathVariable Integer id) {
        return ApiResponse.ok(orderService.cancelOrder(id));
    }

    @PatchMapping("/{id}/start-preparing")
    @PreAuthorize("hasAuthority('ORDER_UPDATE_STATUS') and @orderSecurity.isRestaurantOwnerOf(#id, authentication)")
    public ApiResponse<OrderResponse> startOrderPreparing(@PathVariable Integer id) {
        return ApiResponse.ok(orderService.startOrderPreparing(id));
    }

    @PatchMapping("/{id}/out-for-delivery")
    @PreAuthorize("hasAuthority('ORDER_UPDATE_STATUS') and @orderSecurity.isRestaurantOwnerOf(#id, authentication)")
    public ApiResponse<OrderResponse> orderOutForDelivery(@PathVariable Integer id) {
        return ApiResponse.ok(orderService.markOrderOutForDelivery(id));
    }

    @PatchMapping("/{id}/delivered")
    @PreAuthorize("hasAuthority('DELIVERY_UPDATE')")
    public ApiResponse<OrderResponse> orderDelivered(@PathVariable Integer id) {
        return ApiResponse.ok(orderService.markOrderDelivered(id));
    }

    @GetMapping("/{orderId}/timeline")
    @PreAuthorize("@orderSecurity.canView(#orderId, authentication)")
    public ApiResponse<List<TimelineEntryResponse>> getTimeLineByOrderId(@PathVariable Integer orderId) {
        return ApiResponse.ok(orderService.getTimeLineByOrderId(orderId));
    }

    @PostMapping("/{orderId}/assign-driver")
    public ApiResponse<DriverResponse> assignDriverToOrder(@PathVariable Integer orderId,
                                                           @Valid @RequestBody AssignDriverRequest request) {
        return ApiResponse.ok(assignmentService.assignDriverToOrder(orderId, request));
    }
}
