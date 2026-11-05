package com.example.food_delivery_app.order.dto.driver;

import com.example.food_delivery_app.order.entity.driver.DriverStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateDriverStatusRequest(@NotNull DriverStatus status) {}

