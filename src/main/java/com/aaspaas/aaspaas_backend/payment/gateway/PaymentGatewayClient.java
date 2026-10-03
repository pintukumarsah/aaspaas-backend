package com.aaspaas.aaspaas_backend.payment.gateway;

import java.math.BigDecimal;

public interface PaymentGatewayClient {

    GatewayOrder createOrder(
            BigDecimal amount,
            String receipt
    );

    GatewayRefund createRefund(
            String gatewayPaymentId,
            BigDecimal amount,
            String receipt
    );

    boolean verifyPaymentSignature(
            String gatewayOrderId,
            String gatewayPaymentId,
            String signature
    );

    boolean verifyWebhookSignature(
            String rawBody,
            String signature
    );

    record GatewayOrder(
            String orderId,
            long amountInPaise,
            String currency,
            String receipt
    ) {
    }

    record GatewayRefund(
            String refundId,
            String paymentId,
            long amountInPaise
    ) {
    }
}