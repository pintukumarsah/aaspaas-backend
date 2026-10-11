package com.aaspaas.aaspaas_backend.wallet.provider;

public class PayoutProviderException extends RuntimeException {

    public PayoutProviderException(String message) {
        super(message);
    }

    public PayoutProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
