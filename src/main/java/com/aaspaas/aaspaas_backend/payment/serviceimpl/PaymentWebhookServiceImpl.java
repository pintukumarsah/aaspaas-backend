package com.aaspaas.aaspaas_backend.payment.serviceimpl;

import com.aaspaas.aaspaas_backend.common.exception.BusinessException;
import com.aaspaas.aaspaas_backend.order.entity.Order;
import com.aaspaas.aaspaas_backend.order.repository.OrderRepository;
import com.aaspaas.aaspaas_backend.payment.entity.Payment;
import com.aaspaas.aaspaas_backend.payment.entity.PaymentRefund;
import com.aaspaas.aaspaas_backend.payment.entity.PaymentWebhookEvent;
import com.aaspaas.aaspaas_backend.payment.enums.PaymentStatus;
import com.aaspaas.aaspaas_backend.payment.enums.RefundStatus;
import com.aaspaas.aaspaas_backend.payment.gateway.PaymentGatewayClient;
import com.aaspaas.aaspaas_backend.payment.repository.PaymentRefundRepository;
import com.aaspaas.aaspaas_backend.payment.repository.PaymentRepository;
import com.aaspaas.aaspaas_backend.payment.repository.PaymentWebhookEventRepository;
import com.aaspaas.aaspaas_backend.payment.service.PaymentWebhookService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class PaymentWebhookServiceImpl
        implements PaymentWebhookService {

    private static final String PROVIDER =
            "RAZORPAY";

    private final PaymentGatewayClient
            paymentGatewayClient;

    private final PaymentRepository
            paymentRepository;

    private final PaymentRefundRepository
            paymentRefundRepository;

    private final PaymentWebhookEventRepository
            webhookEventRepository;

    private final OrderRepository
            orderRepository;

    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void processRazorpayWebhook(
            String rawBody,
            String signature,
            String eventId
    ) {

        if (rawBody == null
                || rawBody.isBlank()) {

            throw new BusinessException(
                    "Webhook body is empty",
                    400
            );
        }

        if (!paymentGatewayClient
                .verifyWebhookSignature(
                        rawBody,
                        signature
                )) {

            throw new BusinessException(
                    "Invalid Razorpay webhook signature",
                    400
            );
        }

        try {

            JsonNode root =
                    objectMapper.readTree(
                            rawBody
                    );

            String eventType =
                    root.path("event")
                            .asText(null);

            if (eventType == null
                    || eventType.isBlank()) {

                throw new BusinessException(
                        "Webhook event type missing",
                        400
                );
            }

            String resolvedEventId =
                    eventId;

            if (resolvedEventId == null
                    || resolvedEventId.isBlank()) {

                resolvedEventId =
                        sha256(rawBody);
            }

            /*
             * Idempotency:
             *
             * Same webhook can be delivered again.
             */
            if (webhookEventRepository
                    .existsByProviderAndEventId(
                            PROVIDER,
                            resolvedEventId
                    )) {

                return;
            }

            PaymentWebhookEvent event =
                    new PaymentWebhookEvent();

            event.setProvider(PROVIDER);

            event.setEventId(
                    resolvedEventId
            );

            event.setEventType(
                    eventType
            );

            event.setPayloadHash(
                    sha256(rawBody)
            );

            event.setProcessingStatus(
                    "RECEIVED"
            );

            webhookEventRepository.save(event);

            /*
             * Process only events that affect
             * AASPAAS payment state.
             */
            switch (eventType) {

                case "payment.captured",
                     "order.paid" ->

                        processSuccessfulPayment(
                                root
                        );

                case "payment.failed" ->

                        processFailedPayment(
                                root
                        );

                case "refund.created" ->

                        processRefundCreated(
                                root
                        );

                case "refund.failed" ->

                        processRefundFailed(
                                root
                        );

                default -> {

                    event.setProcessingStatus(
                            "IGNORED"
                    );

                    event.setProcessedAt(
                            OffsetDateTime.now()
                    );

                    webhookEventRepository.save(
                            event
                    );

                    return;
                }
            }

            event.setProcessingStatus(
                    "PROCESSED"
            );

            event.setProcessedAt(
                    OffsetDateTime.now()
            );

            webhookEventRepository.save(event);

        } catch (BusinessException ex) {

            throw ex;

        } catch (Exception ex) {

            throw new BusinessException(
                    "Unable to process payment webhook",
                    400
            );
        }
    }

    private void processSuccessfulPayment(
            JsonNode root
    ) {

        JsonNode paymentNode =
                root.path("payload")
                        .path("payment")
                        .path("entity");

        String gatewayPaymentId =
                paymentNode
                        .path("id")
                        .asText(null);

        String gatewayOrderId =
                paymentNode
                        .path("order_id")
                        .asText(null);

        long gatewayAmount =
                paymentNode
                        .path("amount")
                        .asLong(0);

        if (gatewayPaymentId == null
                || gatewayOrderId == null) {

            return;
        }

        Payment payment =
                paymentRepository
                        .findByGatewayOrderIdForUpdate(
                                gatewayOrderId
                        )
                        .orElse(null);

        if (payment == null) {
            return;
        }

        long expectedAmount =
                payment.getAmount()
                        .movePointRight(2)
                        .longValueExact();

        /*
         * Never trust webhook amount blindly.
         */
        if (expectedAmount
                != gatewayAmount) {

            throw new BusinessException(
                    "Payment amount mismatch",
                    400
            );
        }

        if (payment.getStatus()
                == PaymentStatus.REFUNDED) {

            return;
        }

        payment.setGatewayPaymentId(
                gatewayPaymentId
        );

        payment.setTransactionReference(
                gatewayPaymentId
        );

        payment.setStatus(
                PaymentStatus.SUCCESS
        );

        payment.setPaidAt(
                OffsetDateTime.now()
        );

        paymentRepository.save(payment);

        Order order =
                orderRepository
                        .findByIdForUpdate(
                                payment.getOrder()
                                        .getId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Order not found",
                                        404
                                )
                        );

        /*
         * Payment may succeed just after
         * customer cancellation.
         *
         * Do NOT reopen cancelled order.
         */
        if ("CANCELLED".equalsIgnoreCase(
                order.getOrderStatus()
        )) {

            order.setPaymentStatus(
                    "REFUND_PENDING"
            );

        } else {

            order.setPaymentStatus(
                    "PAID"
            );
        }

        orderRepository.save(order);
    }

    private void processFailedPayment(
            JsonNode root
    ) {

        JsonNode paymentNode =
                root.path("payload")
                        .path("payment")
                        .path("entity");

        String gatewayOrderId =
                paymentNode
                        .path("order_id")
                        .asText(null);

        if (gatewayOrderId == null) {
            return;
        }

        Payment payment =
                paymentRepository
                        .findByGatewayOrderIdForUpdate(
                                gatewayOrderId
                        )
                        .orElse(null);

        if (payment == null) {
            return;
        }

        if (payment.getStatus()
                == PaymentStatus.SUCCESS
                ||
                payment.getStatus()
                        == PaymentStatus.REFUNDED) {

            return;
        }

        payment.setStatus(
                PaymentStatus.FAILED
        );

        paymentRepository.save(payment);

        Order order =
                orderRepository
                        .findByIdForUpdate(
                                payment.getOrder()
                                        .getId()
                        )
                        .orElse(null);

        if (order != null
                && !"PAID".equalsIgnoreCase(
                        order.getPaymentStatus()
                )) {

            order.setPaymentStatus(
                    "FAILED"
            );

            orderRepository.save(order);
        }
    }

    private void processRefundCreated(
            JsonNode root
    ) {

        JsonNode refundNode =
                root.path("payload")
                        .path("refund")
                        .path("entity");

        String gatewayRefundId =
                refundNode
                        .path("id")
                        .asText(null);

        String gatewayPaymentId =
                refundNode
                        .path("payment_id")
                        .asText(null);

        if (gatewayRefundId == null
                || gatewayPaymentId == null) {

            return;
        }

        Payment payment =
                paymentRepository
                        .findByGatewayPaymentId(
                                gatewayPaymentId
                        )
                        .orElse(null);

        if (payment == null) {
            return;
        }

        PaymentRefund refund =
                paymentRefundRepository
                        .findByPaymentId(
                                payment.getId()
                        )
                        .orElse(null);

        if (refund == null) {
            return;
        }

        if (refund.getStatus()
                == RefundStatus.SUCCESS) {

            return;
        }

        refund.setGatewayRefundId(
                gatewayRefundId
        );

        refund.setStatus(
                RefundStatus.SUCCESS
        );

        refund.setProcessedAt(
                OffsetDateTime.now()
        );

        paymentRefundRepository.save(
                refund
        );

        payment.setStatus(
                PaymentStatus.REFUNDED
        );

        paymentRepository.save(payment);

        Order order =
                orderRepository
                        .findByIdForUpdate(
                                payment.getOrder()
                                        .getId()
                        )
                        .orElse(null);

        if (order != null) {

            order.setPaymentStatus(
                    "REFUNDED"
            );

            orderRepository.save(order);
        }
    }

    private void processRefundFailed(
            JsonNode root
    ) {

        JsonNode refundNode =
                root.path("payload")
                        .path("refund")
                        .path("entity");

        String gatewayRefundId =
                refundNode
                        .path("id")
                        .asText(null);

        String gatewayPaymentId =
                refundNode
                        .path("payment_id")
                        .asText(null);

        PaymentRefund refund = null;

        if (gatewayRefundId != null) {

            refund =
                    paymentRefundRepository
                            .findByGatewayRefundId(
                                    gatewayRefundId
                            )
                            .orElse(null);
        }

        if (refund == null
                && gatewayPaymentId != null) {

            Payment payment =
                    paymentRepository
                            .findByGatewayPaymentId(
                                    gatewayPaymentId
                            )
                            .orElse(null);

            if (payment != null) {

                refund =
                        paymentRefundRepository
                                .findByPaymentId(
                                        payment.getId()
                                )
                                .orElse(null);
            }
        }

        if (refund == null) {
            return;
        }

        refund.setStatus(
                RefundStatus.FAILED
        );

        refund.setProcessedAt(
                OffsetDateTime.now()
        );

        paymentRefundRepository.save(refund);

        Payment payment =
                refund.getPayment();

        payment.setStatus(
                PaymentStatus.SUCCESS
        );

        paymentRepository.save(payment);

        Order order =
                orderRepository
                        .findByIdForUpdate(
                                refund.getOrder().getId()
                        )
                        .orElse(null);

        if (order != null) {

            order.setPaymentStatus(
                    "REFUND_FAILED"
            );

            orderRepository.save(order);
        }
    }

    private String sha256(
            String value
    ) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash =
                    digest.digest(
                            value.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            StringBuilder result =
                    new StringBuilder();

            for (byte b : hash) {

                result.append(
                        String.format(
                                "%02x",
                                b
                        )
                );
            }

            return result.toString();

        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Unable to calculate SHA-256",
                    ex
            );
        }
    }
}