package com.example.food_delivery_app.auth.controller;


import com.example.food_delivery_app.auth.dto.AuthResponse;
import com.example.food_delivery_app.auth.dto.RefreshRequest;
import com.example.food_delivery_app.common.ApiResponse;
import com.example.food_delivery_app.common.SuccessCode;
import com.example.food_delivery_app.common.Messages;
import com.example.food_delivery_app.auth.dto.AuthRequest;
import com.example.food_delivery_app.auth.dto.RegisterRequest;
import com.example.food_delivery_app.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final Messages messages;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(messages.get(SuccessCode.USER_REGISTERED), authService.register(request));
    }

    @PostMapping("/refresh-token")
    public ApiResponse<AuthResponse> refreshToken(@Valid @RequestBody RefreshRequest request) {
        return ApiResponse.ok(messages.get(SuccessCode.USER_REFRESH_TOKEN), authService.getRefreshToken(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody RefreshRequest request) {
        authService.logout(authHeader, request.getRefreshToken());
    }

    @PostMapping("/authenticate")
    public ApiResponse<AuthResponse> authenticate(@Valid @RequestBody AuthRequest request) {
        return ApiResponse.ok(messages.get(SuccessCode.USER_AUTHENTICATED), authService.authenticate(request));
    }
}
