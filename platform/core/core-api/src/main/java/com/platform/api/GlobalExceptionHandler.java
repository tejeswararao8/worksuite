package com.platform.api;

import jakarta.validation.ConstraintViolationException;
import com.platform.tenancy.TenantException;
import com.platform.tenancy.TenantSecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Catches ALL exceptions across ALL modules and returns standard ApiResponse error format.
 * Modules never write their own exception handlers.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            errors.put(field, error.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(ApiResponse.error("Validation failed"));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(PlatformException.class)
    public ResponseEntity<ApiResponse<Void>> handlePlatformException(PlatformException ex) {
        log.warn("Platform exception: {}", ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(TenantException.class)
    public ResponseEntity<ApiResponse<Void>> handleTenantException(TenantException ex) {
        log.warn("Tenant business exception: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(TenantSecurityException.class)
    public ResponseEntity<ApiResponse<Void>> handleTenantSecurity(TenantSecurityException ex) {
        // Log at WARN — do NOT expose internal details (tenant ID, DB state) to the caller.
        log.warn("Tenant security violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error("Access denied"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
        // Handle Spring Security access denied exceptions (AuthorizationDeniedException,
        // AccessDeniedException) without importing spring-security in core-api.
        String exClass = ex.getClass().getName();
        if (exClass.contains("AccessDeniedException") || exClass.contains("AuthorizationDeniedException")) {
            log.warn("Access denied: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied"));
        }
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("An unexpected error occurred"));
    }
}
