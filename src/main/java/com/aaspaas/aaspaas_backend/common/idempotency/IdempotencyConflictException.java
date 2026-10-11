package com.aaspaas.aaspaas_backend.common.idempotency;

public class IdempotencyConflictException extends RuntimeException {

    public IdempotencyConflictException(String message) {
        super(message);
    }
}