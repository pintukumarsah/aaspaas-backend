package com.aaspaas.aaspaas_backend.payment.service;

import com.aaspaas.aaspaas_backend.payment.dto.CreatePaymentRequest;
import com.aaspaas.aaspaas_backend.payment.dto.PaymentResponse;
import com.aaspaas.aaspaas_backend.payment.dto.PaymentWebhookRequest;
import com.aaspaas.aaspaas_backend.payment.dto.RazorpayOrderResponse;
import com.aaspaas.aaspaas_backend.payment.dto.RazorpayVerifyRequest;

import java.util.List;

public interface PaymentService {

    PaymentResponse createPayment(
            CreatePaymentRequest request,
            String authenticatedPhone
    );

    PaymentResponse getPayment(
            Long paymentId,
            String authenticatedPhone
    );

    List<PaymentResponse> getPaymentsForOrder(
            Long orderId,
            String authenticatedPhone
    );

    /*
     * Legacy/internal webhook lifecycle.
     *
     * Kept for service compatibility.
     * Public Razorpay webhook should use
     * PaymentWebhookService with signature verification.
     */
    PaymentResponse processWebhook(
            PaymentWebhookRequest request
    );

    /*
     * Razorpay
     */

    RazorpayOrderResponse createRazorpayPayment(
            Long orderId,
            String authenticatedPhone
    );

    void verifyRazorpayPayment(
            String authenticatedPhone,
            RazorpayVerifyRequest request
    );

    void initiateRefund(
            Long orderId,
            String reason
    );
}