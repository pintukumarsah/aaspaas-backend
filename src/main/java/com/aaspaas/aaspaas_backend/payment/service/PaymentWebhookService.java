package com.aaspaas.aaspaas_backend.payment.service;

public interface PaymentWebhookService {

    void processRazorpayWebhook(
            String rawBody,
            String signature,
            String eventId
    );
}