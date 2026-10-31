package com.example.food_delivery_app.handler;

import com.example.food_delivery_app.exceptions.AppException;
import com.example.food_delivery_app.common.ErrorCode;
import com.example.food_delivery_app.common.Messages;
import com.example.food_delivery_app.common.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import org.springframework.security.access.AccessDeniedException;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * The one and only exception handler for the whole application.
 * It inherits from ResponseEntityExceptionHandler to handle Spring MVC exceptions (like 405, 415, wrong json format and missing params).
 * and it doesn't be converted to 500 wrongly, but instead it returns a proper status code and a unified error response.
 * and it also handles our custom AppException (NotFound, Business, Conflict, Unauthorized...).
 *
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final Messages messages;

    // One place that builds every error response.
    private ResponseEntity<ApiResponse<Void>> build(ErrorCode code, Object... args) {
        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.error(code.name(), messages.get(code, args)));
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        log.warn("{}", ex.getErrorCode());
        return build(ex.getErrorCode(), (Object[]) ex.getArgs());
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(f -> errors.putIfAbsent(f.getField(), f.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(headers)
                .body(ApiResponse.error(ErrorCode.VALIDATION_FAILED.name(),
                        messages.get(ErrorCode.VALIDATION_FAILED), errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getConstraintViolations()
                .forEach(v -> errors.putIfAbsent(v.getPropertyPath().toString(), v.getMessage()));
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ErrorCode.VALIDATION_FAILED.name(),
                        messages.get(ErrorCode.VALIDATION_FAILED), errors));
    }

    // DB constraint errors. Unique (23505) and foreign key (23503) are conflicts: 409.
    // Anything else (for example NOT NULL, 23502) means OUR code is wrong, so it stays a 500.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        if (ex.getMostSpecificCause() instanceof SQLException sql
                && Set.of("23505", "23503").contains(sql.getSQLState())) {
            log.warn("Data conflict: {}", sql.getMessage());     // logged, NOT sent to the client
            return build(ErrorCode.DATA_CONFLICT);
        }
        return handleUnexpected(ex);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body,
                                                                       HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        String reason = status != null ? status.getReasonPhrase() : "Error";
        if (statusCode.is5xxServerError()) {
            log.error("Server error", ex);
        }
        return ResponseEntity.status(statusCode).headers(headers)
                .body(ApiResponse.error("HTTP_" + statusCode.value(), reason));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return build(ErrorCode.INTERNAL_ERROR);
    }

    // wrong email or password -> 401 (same message for both, so attackers can't tell which one is wrong)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Bad credentials");
        return build(ErrorCode.INVALID_CREDENTIALS);
    }

    // any other authentication failure (disabled account, locked, ...) -> 401
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getClass().getSimpleName());
        return build(ErrorCode.UNAUTHORIZED);
    }

    // logged in but not allowed (@PreAuthorize) -> 403
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied");
        return build(ErrorCode.FORBIDDEN);
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceFailure(DataAccessResourceFailureException ex) {
        return build(ErrorCode.SERVICE_UNAVAILABLE);
    }


}