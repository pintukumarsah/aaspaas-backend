package com.aaspaas.aaspaas_backend.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // =========================================================
    // BUSINESS EXCEPTION
    // =========================================================

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();

        response.put("status", ex.getStatusCode());
        response.put("message", ex.getMessage());
        response.put("path", request.getRequestURI());
        response.put("timestamp", OffsetDateTime.now());

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(response);
    }


    // =========================================================
    // RESOURCE NOT FOUND EXCEPTION
    // =========================================================

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();

        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("message", ex.getMessage());
        response.put("path", request.getRequestURI());
        response.put("timestamp", OffsetDateTime.now());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }


    // =========================================================
    // VALIDATION EXCEPTION
    // =========================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        Map<String, Object> response = new HashMap<>();

        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("message", "Validation failed");
        response.put("errors", errors);
        response.put("path", request.getRequestURI());
        response.put("timestamp", OffsetDateTime.now());

        return ResponseEntity
                .badRequest()
                .body(response);
    }


    // =========================================================
    // RUNTIME EXCEPTION
    // =========================================================
    //
    // Temporary / development-friendly handler.
    //
    // Agar kisi service mein abhi bhi:
    //
    // throw new RuntimeException("Some message");
    //
    // hai, to actual message Postman mein dikhega.
    //
    // =========================================================

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();

        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put(
                "message",
                ex.getMessage() != null
                        ? ex.getMessage()
                        : "Runtime error"
        );
        response.put("path", request.getRequestURI());
        response.put("timestamp", OffsetDateTime.now());

        return ResponseEntity
                .badRequest()
                .body(response);
    }


    // =========================================================
    // GENERIC EXCEPTION
    // =========================================================
    //
    // Unexpected errors ke liye.
    // User ko internal technical details nahi dikhayenge.
    //
    // =========================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();

        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("message", "Internal server error");
        response.put("path", request.getRequestURI());
        response.put("timestamp", OffsetDateTime.now());

        return ResponseEntity
                .internalServerError()
                .body(response);
    }
}