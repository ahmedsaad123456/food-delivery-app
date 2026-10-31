package com.example.food_delivery_app.restaurant.dto;

import com.example.food_delivery_app.validation.EgyptianPhone;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;


@Builder
public record CreateRestaurantRequest(

        @NotBlank(message = "name is required")
        @Size(max = 150, message = "name must be at most 150 characters")
        String name,

        @Size(max = 2000, message = "description must be at most 2000 characters")
        String description,

        @NotNull(message = "categoryId is required")
        Integer categoryId,

        @NotBlank(message = "address is required")
        @Size(max = 255, message = "address must be at most 255 characters")
        String address,

        @EgyptianPhone
        String phone,

        Boolean open


) {}