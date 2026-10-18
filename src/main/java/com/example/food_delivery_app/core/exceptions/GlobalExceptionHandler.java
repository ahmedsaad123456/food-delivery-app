package com.example.food_delivery_app.core.exceptions;

import com.example.food_delivery_app.core.utils.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The one and only exception handler for the whole application.
 *
 * It inherits from ResponseEntityExceptionHandler to handle Spring MVC exceptions (like 405, 415, wrong json format and missing params).
 * and it doesn't be converted to 500 wrongly, but instead it returns a proper status code and a unified error response.
 * and it also handles our custom AppException (NotFound, Business, Conflict, Unauthorized...).
 *
 */
@Slf4j // Lombok annotation to automatically generate a logger for this class.
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // Handle our custom AppException (NotFound, Business, Conflict, Unauthorized...).
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        log.warn("{}: {}", ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.status(ex.getStatus())
                .body(ApiResponse.error(ex.getErrorCode(), ex.getMessage()));
    }

    // 400: any validation error on @RequestBody (for example, when using @Valid or @Validated).
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(f -> errors.putIfAbsent(f.getField(), f.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(headers)
                .body(ApiResponse.error("VALIDATION_FAILED", "Validation failed", errors));
    }

    // 400: any validation error on @RequestParam, @PathVariable, @RequestHeader (for example, when using @Valid or @Validated).
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolation(ConstraintViolationException ex) {
        // Use LinkedHashMap to preserve the order of the errors.
        Map<String, String> errors = new LinkedHashMap<>();

        // Iterate over the constraint violations and add them to the errors map.
        ex.getConstraintViolations()
                .forEach(v -> errors.putIfAbsent(v.getPropertyPath().toString(), v.getMessage()));
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("VALIDATION_FAILED", "Validation failed", errors));
    }

    // 400, 404, 405, 415, 500: any other Spring MVC exception (like 405, 415, wrong json format and missing params).
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        String reason = status != null ? status.getReasonPhrase() : "Error";
        if (statusCode.is5xxServerError()) {
            log.error("Server error", ex);
        }
        return ResponseEntity.status(statusCode).headers(headers)
                .body(ApiResponse.error("HTTP_" + statusCode.value(), reason));
    }

    // 500: any unexpected error that is not handled by the above handlers.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "Something went wrong. Please try again later."));
    }
}
