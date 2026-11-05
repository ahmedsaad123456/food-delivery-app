package com.example.food_delivery_app.order.dto.order;

import com.example.food_delivery_app.order.entity.order.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChangeOrderStatusRequest(@NotNull OrderStatus status, @Size(max = 500) String note) {}

