package com.aaspaas.aaspaas_backend.payment.controller;

import com.aaspaas.aaspaas_backend.payment.service.PaymentWebhookService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments/webhook")
@RequiredArgsConstructor
public class RazorpayWebhookController {

    private final PaymentWebhookService
            paymentWebhookService;

    @PostMapping("/razorpay")
    public ResponseEntity<Void> receiveWebhook(
            @RequestHeader(
                    value = "X-Razorpay-Signature",
                    required = false
            )
            String signature,

            @RequestHeader(
                    value = "X-Razorpay-Event-Id",
                    required = false
            )
            String eventId,

            @RequestBody
            String rawBody
    ) {

        paymentWebhookService
                .processRazorpayWebhook(
                        rawBody,
                        signature,
                        eventId
                );

        return ResponseEntity.ok().build();
    }

    
}