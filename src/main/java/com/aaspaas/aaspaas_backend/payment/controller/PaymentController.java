package com.aaspaas.aaspaas_backend.payment.controller;

import com.aaspaas.aaspaas_backend.payment.dto.CreatePaymentRequest;
import com.aaspaas.aaspaas_backend.payment.dto.PaymentResponse;
import com.aaspaas.aaspaas_backend.payment.dto.PaymentWebhookRequest;
import com.aaspaas.aaspaas_backend.payment.dto.RazorpayOrderResponse;
import com.aaspaas.aaspaas_backend.payment.dto.RazorpayVerifyRequest;
import com.aaspaas.aaspaas_backend.payment.service.PaymentService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;


    // =========================================================
    // INTERNAL PAYMENT CREATION
    // =========================================================

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(

            @Valid
            @RequestBody
            CreatePaymentRequest request,

            Authentication authentication
    ) {

        PaymentResponse response =
                paymentService.createPayment(
                        request,
                        authentication.getName()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // =========================================================
    // GET SINGLE PAYMENT
    // =========================================================

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(

            @PathVariable
            Long paymentId,

            Authentication authentication
    ) {

        PaymentResponse response =
                paymentService.getPayment(
                        paymentId,
                        authentication.getName()
                );

        return ResponseEntity.ok(
                response
        );
    }


    // =========================================================
    // GET ORDER PAYMENTS
    // =========================================================

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentResponse>>
    getOrderPayments(

            @PathVariable
            Long orderId,

            Authentication authentication
    ) {

        List<PaymentResponse> response =
                paymentService.getPaymentsForOrder(
                        orderId,
                        authentication.getName()
                );

        return ResponseEntity.ok(
                response
        );
    }


    // =========================================================
    // CREATE RAZORPAY ORDER
    // =========================================================

    @PostMapping("/razorpay/create/{orderId}")
    public ResponseEntity<RazorpayOrderResponse>
    createRazorpayPayment(

            @PathVariable
            Long orderId,

            Authentication authentication
    ) {

        RazorpayOrderResponse response =
                paymentService.createRazorpayPayment(
                        orderId,
                        authentication.getName()
                );

        return ResponseEntity.ok(
                response
        );
    }


    // =========================================================
    // VERIFY RAZORPAY PAYMENT
    // =========================================================

    @PostMapping("/razorpay/verify")
    public ResponseEntity<Void>
    verifyRazorpayPayment(

            @Valid
            @RequestBody
            RazorpayVerifyRequest request,

            Authentication authentication
    ) {

        paymentService.verifyRazorpayPayment(
                authentication.getName(),
                request
        );

        return ResponseEntity.ok()
                .build();
    }


    // =========================================================
    // LEGACY INTERNAL WEBHOOK
    // =========================================================
    /*
     * IMPORTANT:
     *
     * This endpoint is intentionally NOT used for
     * real Razorpay webhook traffic.
     *
     * Real Razorpay webhook:
     *
     * /api/payments/webhook/razorpay
     *
     * is handled by RazorpayWebhookController
     * and PaymentWebhookService.
     *
     * This endpoint is kept only for backward compatibility
     * with the Part 25 internal payment lifecycle.
     */

    @PostMapping("/internal-webhook")
    public ResponseEntity<PaymentResponse>
    processInternalWebhook(

            @Valid
            @RequestBody
            PaymentWebhookRequest request
    ) {

        PaymentResponse response =
                paymentService.processWebhook(
                        request
                );

        return ResponseEntity.ok(
                response
        );
    }
}