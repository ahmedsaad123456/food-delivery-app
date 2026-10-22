package com.example.food_delivery_app.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final String message;
    private final String errorCode;
    private final T data;
    @Builder.Default
    private final Instant timestamp = Instant.now();

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder().data(data).build();
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return ApiResponse.<T>builder().message(message).data(data).build();
    }

    public static <T> ApiResponse<T> error(String errorCode, String message) {
        return ApiResponse.<T>builder().errorCode(errorCode).message(message).build();
    }

    public static <T> ApiResponse<T> error(String errorCode, String message, T details) {
        return ApiResponse.<T>builder().errorCode(errorCode).message(message).data(details).build();
    }
}
