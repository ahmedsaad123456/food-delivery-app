package com.example.food_delivery_app.features.auth.controller;

import com.example.food_delivery_app.core.exceptions.ResourceNotFoundException;
import com.example.food_delivery_app.core.utils.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class TestController {

    @GetMapping("/ping")
    public ApiResponse<String> ping() {
        return ApiResponse.ok("Server is working", "pong");
    }

    @GetMapping("/boom")
    public ApiResponse<String> boom() {
        throw new ResourceNotFoundException("Test exception");
    }
}
