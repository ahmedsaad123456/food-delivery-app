package com.example.food_delivery_app.auth.controller;


import com.example.food_delivery_app.auth.dto.AuthResponse;
import com.example.food_delivery_app.common.ApiResponse;
import com.example.food_delivery_app.common.SuccessCode;
import com.example.food_delivery_app.common.Messages;
import com.example.food_delivery_app.auth.dto.AuthRequest;
import com.example.food_delivery_app.auth.dto.RegisterRequest;
import com.example.food_delivery_app.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final Messages messages;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(messages.get(SuccessCode.USER_REGISTERED), authService.register(request));
    }

    @PostMapping("/authenticate")
    public ApiResponse<AuthResponse> authenticate(@Valid @RequestBody AuthRequest request) {
        return ApiResponse.ok(messages.get(SuccessCode.USER_AUTHENTICATED), authService.authenticate(request));
    }
}
