package io.github.nguyenvu.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST APIs.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        return ResponseEntity.badRequest().body(Map.of(
            "timestamp", LocalDateTime.now(),
            "status", HttpStatus.BAD_REQUEST.value(),
            "error", "Validation failed",
            "details", errors
        ));
    }

    /**
     * Handle illegal argument exceptions.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex) {
        
        log.warn("Illegal argument: {}", ex.getMessage());
        
        return ResponseEntity.badRequest().body(Map.of(
            "timestamp", LocalDateTime.now(),
            "status", HttpStatus.BAD_REQUEST.value(),
            "error", "Invalid request",
            "message", ex.getMessage()
        ));
    }

    /**
     * Handle type mismatch errors (e.g., invalid date format).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        
        String error = String.format("Invalid value '%s' for parameter '%s'", 
            ex.getValue(), ex.getName());
        
        return ResponseEntity.badRequest().body(Map.of(
            "timestamp", LocalDateTime.now(),
            "status", HttpStatus.BAD_REQUEST.value(),
            "error", "Type mismatch",
            "message", error
        ));
    }

    /**
     * Handle all other exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericError(Exception ex) {
        log.error("Unexpected error", ex);
        
        return ResponseEntity.internalServerError().body(Map.of(
            "timestamp", LocalDateTime.now(),
            "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "error", "Internal server error",
            "message", "An unexpected error occurred"
        ));
    }
}
