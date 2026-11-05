package com.example.food_delivery_app.order.dto.driver;

import com.example.food_delivery_app.validation.EgyptianPhone;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateDriverRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @EgyptianPhone String phone,
        @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,
        @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude) {}