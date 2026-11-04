package com.example.food_delivery_app.menu.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
public record UpdateMenuItemRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 2000) String description,

        Integer stock,
        @NotBlank @Size(max = 100) String section,
        @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal price,
        @NotNull Boolean available
) {}