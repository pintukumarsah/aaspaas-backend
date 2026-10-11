// package com.aaspaas.aaspaas_backend.common.exception;

// import jakarta.servlet.http.HttpServletRequest;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.MethodArgumentNotValidException;
// import org.springframework.web.bind.annotation.ExceptionHandler;
// import org.springframework.web.bind.annotation.RestControllerAdvice;

// import java.time.OffsetDateTime;
// import java.util.HashMap;
// import java.util.Map;

// @RestControllerAdvice
// public class GlobalExceptionHandler {

//     // =========================================================
//     // BUSINESS EXCEPTION
//     // =========================================================

//     @ExceptionHandler(BusinessException.class)
//     public ResponseEntity<Map<String, Object>> handleBusinessException(
//             BusinessException ex,
//             HttpServletRequest request) {

//         Map<String, Object> response = new HashMap<>();

//         response.put("status", ex.getStatusCode());
//         response.put("message", ex.getMessage());
//         response.put("path", request.getRequestURI());
//         response.put("timestamp", OffsetDateTime.now());

//         return ResponseEntity
//                 .status(ex.getStatusCode())
//                 .body(response);
//     }


//     // =========================================================
//     // RESOURCE NOT FOUND EXCEPTION
//     // =========================================================

//     @ExceptionHandler(ResourceNotFoundException.class)
//     public ResponseEntity<Map<String, Object>> handleResourceNotFound(
//             ResourceNotFoundException ex,
//             HttpServletRequest request) {

//         Map<String, Object> response = new HashMap<>();

//         response.put("status", HttpStatus.NOT_FOUND.value());
//         response.put("message", ex.getMessage());
//         response.put("path", request.getRequestURI());
//         response.put("timestamp", OffsetDateTime.now());

//         return ResponseEntity
//                 .status(HttpStatus.NOT_FOUND)
//                 .body(response);
//     }


//     // =========================================================
//     // VALIDATION EXCEPTION
//     // =========================================================

//     @ExceptionHandler(MethodArgumentNotValidException.class)
//     public ResponseEntity<Map<String, Object>> handleValidationException(
//             MethodArgumentNotValidException ex,
//             HttpServletRequest request) {

//         Map<String, String> errors = new HashMap<>();

//         ex.getBindingResult()
//                 .getFieldErrors()
//                 .forEach(error ->
//                         errors.put(
//                                 error.getField(),
//                                 error.getDefaultMessage()
//                         )
//                 );

//         Map<String, Object> response = new HashMap<>();

//         response.put("status", HttpStatus.BAD_REQUEST.value());
//         response.put("message", "Validation failed");
//         response.put("errors", errors);
//         response.put("path", request.getRequestURI());
//         response.put("timestamp", OffsetDateTime.now());

//         return ResponseEntity
//                 .badRequest()
//                 .body(response);
//     }


//     // =========================================================
//     // RUNTIME EXCEPTION
//     // =========================================================
//     //
//     // Temporary / development-friendly handler.
//     //
//     // Agar kisi service mein abhi bhi:
//     //
//     // throw new RuntimeException("Some message");
//     //
//     // hai, to actual message Postman mein dikhega.
//     //
//     // =========================================================

//     @ExceptionHandler(RuntimeException.class)
//     public ResponseEntity<Map<String, Object>> handleRuntimeException(
//             RuntimeException ex,
//             HttpServletRequest request) {

//         Map<String, Object> response = new HashMap<>();

//         response.put("status", HttpStatus.BAD_REQUEST.value());
//         response.put(
//                 "message",
//                 ex.getMessage() != null
//                         ? ex.getMessage()
//                         : "Runtime error"
//         );
//         response.put("path", request.getRequestURI());
//         response.put("timestamp", OffsetDateTime.now());

//         return ResponseEntity
//                 .badRequest()
//                 .body(response);
//     }


//     // =========================================================
//     // GENERIC EXCEPTION
//     // =========================================================
//     //
//     // Unexpected errors ke liye.
//     // User ko internal technical details nahi dikhayenge.
//     //
//     // =========================================================

//     @ExceptionHandler(Exception.class)
//     public ResponseEntity<Map<String, Object>> handleGenericException(
//             Exception ex,
//             HttpServletRequest request) {

//         Map<String, Object> response = new HashMap<>();

//         response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
//         response.put("message", "Internal server error");
//         response.put("path", request.getRequestURI());
//         response.put("timestamp", OffsetDateTime.now());

//         return ResponseEntity
//                 .internalServerError()
//                 .body(response);
//     }
// }

package com.aaspaas.aaspaas_backend.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String CORRELATION_HEADER =
            "X-Correlation-ID";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND",
                safeMessage(ex.getMessage(), "Requested resource was not found"),
                request,
                Map.of()
        );
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                "BUSINESS_RULE_VIOLATION",
                safeMessage(ex.getMessage(), "The operation cannot be completed"),
                request,
                Map.of()
        );
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(
            ConflictException ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.CONFLICT,
                "RESOURCE_CONFLICT",
                safeMessage(ex.getMessage(), "The request conflicts with the current state"),
                request,
                Map.of()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(
                    fieldError.getField(),
                    safeMessage(
                            fieldError.getDefaultMessage(),
                            "Invalid value"
                    )
            );
        }

        return buildError(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_FAILED",
                "One or more request fields are invalid",
                request,
                errors
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        // Avoid exposing internal object paths or rejected values.
        return buildError(
                HttpStatus.BAD_REQUEST,
                "CONSTRAINT_VIOLATION",
                "One or more request parameters are invalid",
                request,
                Map.of()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBody(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST_BODY",
                "Request body is missing or contains invalid JSON or field types",
                request,
                Map.of()
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                "MISSING_REQUEST_PARAMETER",
                "A required request parameter is missing",
                request,
                Map.of(ex.getParameterName(), "This parameter is required")
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        String parameter = ex.getName();

        return buildError(
                HttpStatus.BAD_REQUEST,
                "INVALID_PARAMETER_TYPE",
                "A request parameter has an invalid type",
                request,
                Map.of(parameter, "Invalid value or type")
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.METHOD_NOT_ALLOWED,
                "METHOD_NOT_ALLOWED",
                "This HTTP method is not supported for the requested endpoint",
                request,
                Map.of()
        );
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoHandlerFound(
            NoHandlerFoundException ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.NOT_FOUND,
                "ENDPOINT_NOT_FOUND",
                "The requested endpoint was not found",
                request,
                Map.of()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        /*
         * Do not return SQL, constraint names or database messages.
         * Inspect the cause internally when diagnosing the problem.
         */
        log.warn(
                "Database integrity violation correlationId={} path={}",
                request.getHeader(CORRELATION_HEADER),
                request.getRequestURI()
        );

        return buildError(
                HttpStatus.CONFLICT,
                "DATA_INTEGRITY_CONFLICT",
                "The request conflicts with existing data",
                request,
                Map.of()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error(
                "Unhandled API exception correlationId={} method={} path={}",
                request.getHeader(CORRELATION_HEADER),
                request.getMethod(),
                request.getRequestURI(),
                ex
        );

        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "An unexpected server error occurred",
                request,
                Map.of()
        );
    }

    private ResponseEntity<ApiErrorResponse> buildError(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request,
            Map<String, String> validationErrors
    ) {
        String correlationId =
                request.getHeader(CORRELATION_HEADER);

        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                request.getRequestURI(),
                correlationId,
                validationErrors
        );

        return ResponseEntity
                .status(status)
                .body(body);
    }

    private String safeMessage(String message, String fallback) {
        if (message == null || message.isBlank()) {
            return fallback;
        }

        return message;
    }
}