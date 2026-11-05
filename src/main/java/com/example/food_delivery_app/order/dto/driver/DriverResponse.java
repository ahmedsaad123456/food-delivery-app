package com.example.food_delivery_app.order.dto.driver;

import com.example.food_delivery_app.order.entity.driver.DriverStatus;

import java.math.BigDecimal;

public record DriverResponse(Integer id, String name, String phone, DriverStatus status,
                             BigDecimal latitude, BigDecimal longitude) {}
