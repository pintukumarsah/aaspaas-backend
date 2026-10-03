package com.aaspaas.aaspaas_backend.payment.serviceimpl;

import com.aaspaas.aaspaas_backend.common.exception.BusinessException;
import com.aaspaas.aaspaas_backend.order.entity.Order;
import com.aaspaas.aaspaas_backend.order.repository.OrderRepository;
import com.aaspaas.aaspaas_backend.payment.dto.CreatePaymentRequest;
import com.aaspaas.aaspaas_backend.payment.dto.PaymentResponse;
import com.aaspaas.aaspaas_backend.payment.dto.PaymentWebhookRequest;
import com.aaspaas.aaspaas_backend.payment.dto.RazorpayOrderResponse;
import com.aaspaas.aaspaas_backend.payment.dto.RazorpayVerifyRequest;
import com.aaspaas.aaspaas_backend.payment.entity.Payment;
import com.aaspaas.aaspaas_backend.payment.entity.PaymentRefund;
import com.aaspaas.aaspaas_backend.payment.enums.PaymentMethod;
import com.aaspaas.aaspaas_backend.payment.enums.PaymentStatus;
import com.aaspaas.aaspaas_backend.payment.enums.RefundStatus;
import com.aaspaas.aaspaas_backend.payment.gateway.PaymentGatewayClient;
import com.aaspaas.aaspaas_backend.payment.repository.PaymentRefundRepository;
import com.aaspaas.aaspaas_backend.payment.repository.PaymentRepository;
import com.aaspaas.aaspaas_backend.payment.service.PaymentService;
import com.aaspaas.aaspaas_backend.user.entity.User;
import com.aaspaas.aaspaas_backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl
        implements PaymentService {


    private final PaymentRepository paymentRepository;

    private final PaymentRefundRepository
            paymentRefundRepository;

    private final OrderRepository orderRepository;

    private final UserRepository userRepository;

    private final PaymentGatewayClient
            paymentGatewayClient;


    @Value("${payment.razorpay.key-id:}")
    private String razorpayKeyId;


    // =========================================================
    // INTERNAL PAYMENT CREATION
    // =========================================================

    @Override
    @Transactional
    public PaymentResponse createPayment(
            CreatePaymentRequest request,
            String authenticatedPhone
    ) {

        User user =
                userRepository
                        .findByPhone(
                                authenticatedPhone
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "User not found",
                                        404
                                )
                        );


        Order order =
                orderRepository
                        .findByIdForUpdate(
                                request.getOrderId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Order not found",
                                        404
                                )
                        );


        // -----------------------------------------------------
        // OWNERSHIP
        // -----------------------------------------------------

        if (!order.getCustomer()
                .getId()
                .equals(user.getId())) {

            throw new BusinessException(
                    "You are not authorized to pay this order",
                    403
            );
        }


        // -----------------------------------------------------
        // ALREADY PAID
        // -----------------------------------------------------

        if ("PAID".equalsIgnoreCase(
                order.getPaymentStatus()
        )) {

            throw new BusinessException(
                    "Order is already paid",
                    400
            );
        }


        // -----------------------------------------------------
        // CANCELLED ORDER
        // -----------------------------------------------------

        if ("CANCELLED".equalsIgnoreCase(
                order.getOrderStatus()
        )) {

            throw new BusinessException(
                    "Payment is not allowed for cancelled order",
                    400
            );
        }


        // -----------------------------------------------------
        // REFUNDED ORDER
        // -----------------------------------------------------

        if ("REFUNDED".equalsIgnoreCase(
                order.getPaymentStatus()
        )) {

            throw new BusinessException(
                    "Payment is not allowed for refunded order",
                    400
            );
        }


        // -----------------------------------------------------
        // EXISTING PAYMENT / IDEMPOTENCY
        // -----------------------------------------------------

        Payment existingPayment =
                paymentRepository
                        .findFirstByOrderIdOrderByCreatedAtDesc(
                                order.getId()
                        )
                        .orElse(null);


        if (existingPayment != null) {

            if (existingPayment.getStatus()
                    == PaymentStatus.INITIATED
                    ||
                existingPayment.getStatus()
                    == PaymentStatus.PENDING) {

                return mapToResponse(
                        existingPayment
                );
            }


            if (existingPayment.getStatus()
                    == PaymentStatus.SUCCESS) {

                throw new BusinessException(
                        "Order payment is already successful",
                        400
                );
            }
        }


        // =====================================================
        // COD
        // =====================================================

        if (request.getPaymentMethod()
                == PaymentMethod.COD) {

            Payment payment =
                    new Payment();

            payment.setOrder(order);

            payment.setUser(user);

            payment.setAmount(
                    order.getTotalAmount()
            );

            payment.setPaymentMethod(
                    PaymentMethod.COD
            );

            payment.setStatus(
                    PaymentStatus.SUCCESS
            );

            payment.setTransactionReference(
                    generateTransactionReference()
            );

            payment.setPaidAt(
                    OffsetDateTime.now()
            );

            payment =
                    paymentRepository.save(
                            payment
                    );


            order.setPaymentStatus(
                    "COD"
            );

            orderRepository.save(
                    order
            );


            return mapToResponse(
                    payment
            );
        }


        // =====================================================
        // ONLINE INTERNAL PAYMENT
        // =====================================================

        Payment payment =
                new Payment();

        payment.setOrder(order);

        payment.setUser(user);

        payment.setAmount(
                order.getTotalAmount()
        );

        payment.setPaymentMethod(
                request.getPaymentMethod()
        );

        payment.setStatus(
                PaymentStatus.INITIATED
        );

        payment.setTransactionReference(
                generateTransactionReference()
        );

        payment =
                paymentRepository.save(
                        payment
                );


        order.setPaymentStatus(
                "INITIATED"
        );

        orderRepository.save(
                order
        );


        return mapToResponse(
                payment
        );
    }


    // =========================================================
    // GET PAYMENT
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(
            Long paymentId,
            String authenticatedPhone
    ) {

        User user =
                userRepository
                        .findByPhone(
                                authenticatedPhone
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "User not found",
                                        404
                                )
                        );


        Payment payment =
                paymentRepository
                        .findById(
                                paymentId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Payment not found",
                                        404
                                )
                        );


        if (!payment.getUser()
                .getId()
                .equals(user.getId())) {

            throw new BusinessException(
                    "You are not authorized to view this payment",
                    403
            );
        }


        return mapToResponse(
                payment
        );
    }


    // =========================================================
    // GET ORDER PAYMENTS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsForOrder(
            Long orderId,
            String authenticatedPhone
    ) {

        User user =
                userRepository
                        .findByPhone(
                                authenticatedPhone
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "User not found",
                                        404
                                )
                        );


        Order order =
                orderRepository
                        .findById(
                                orderId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Order not found",
                                        404
                                )
                        );


        if (!order.getCustomer()
                .getId()
                .equals(user.getId())) {

            throw new BusinessException(
                    "You are not authorized to view this order payments",
                    403
            );
        }


        return paymentRepository
                .findByOrderIdOrderByCreatedAtDesc(
                        orderId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // =========================================================
    // RAZORPAY ORDER CREATION
    // =========================================================

    @Override
    @Transactional
    public RazorpayOrderResponse createRazorpayPayment(
            Long orderId,
            String authenticatedPhone
    ) {

        User user =
                userRepository
                        .findByPhone(authenticatedPhone)
                        .orElseThrow(() ->
                                new BusinessException(
                                        "User not found",
                                        404
                                )
                        );


        Order order =
                orderRepository
                        .findByIdForUpdate(
                                orderId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Order not found",
                                        404
                                )
                        );


        // -----------------------------------------------------
        // OWNERSHIP
        // -----------------------------------------------------

        if (!order.getCustomer()
                .getId()
                .equals(user.getId())) {

            throw new BusinessException(
                    "You are not allowed to pay for this order",
                    403
            );
        }


        // -----------------------------------------------------
        // CANCELLED
        // -----------------------------------------------------

        if ("CANCELLED".equalsIgnoreCase(
                order.getOrderStatus()
        )) {

            throw new BusinessException(
                    "Cancelled order cannot be paid",
                    409
            );
        }


        // -----------------------------------------------------
        // ALREADY PAID
        // -----------------------------------------------------

        if ("PAID".equalsIgnoreCase(
                order.getPaymentStatus()
        )) {

            throw new BusinessException(
                    "Order is already paid",
                    409
            );
        }


        // -----------------------------------------------------
        // INVALID AMOUNT
        // -----------------------------------------------------

        if (order.getTotalAmount() == null
                ||
            order.getTotalAmount()
                    .compareTo(BigDecimal.ZERO) <= 0) {

            throw new BusinessException(
                    "Order amount must be greater than zero",
                    400
            );
        }


        // -----------------------------------------------------
        // EXISTING ACTIVE RAZORPAY PAYMENT
        // -----------------------------------------------------

        Payment existingPayment =
                paymentRepository
                        .findFirstByOrderIdOrderByCreatedAtDesc(
                                orderId
                        )
                        .orElse(null);


        if (existingPayment != null
                && existingPayment.getGatewayOrderId() != null
                && (
                    existingPayment.getStatus()
                            == PaymentStatus.INITIATED
                    ||
                    existingPayment.getStatus()
                            == PaymentStatus.PENDING
                )) {

            return buildRazorpayOrderResponse(
                    existingPayment,
                    order
            );
        }


        // -----------------------------------------------------
        // CREATE INTERNAL PAYMENT
        // -----------------------------------------------------

        Payment payment =
                new Payment();

        payment.setOrder(order);

        payment.setUser(user);

        payment.setAmount(
                order.getTotalAmount()
        );

        payment.setPaymentMethod(
                PaymentMethod.UPI
        );

        payment.setStatus(
                PaymentStatus.INITIATED
        );

        payment.setTransactionReference(
                generateTransactionReference()
        );

        payment =
                paymentRepository.save(
                        payment
                );


        // -----------------------------------------------------
        // CREATE RAZORPAY ORDER
        // -----------------------------------------------------

        PaymentGatewayClient.GatewayOrder
                gatewayOrder;

        try {

            gatewayOrder =
                    paymentGatewayClient.createOrder(
                            order.getTotalAmount(),
                            order.getOrderNumber()
                    );

        } catch (RuntimeException ex) {

            payment.setStatus(
                    PaymentStatus.FAILED
            );

            paymentRepository.save(
                    payment
            );

            throw ex;
        }


        // -----------------------------------------------------
        // SAVE GATEWAY ORDER
        // -----------------------------------------------------

        payment.setGatewayOrderId(
                gatewayOrder.orderId()
        );

        payment.setStatus(
                PaymentStatus.PENDING
        );

        paymentRepository.save(
                payment
        );


        order.setPaymentStatus(
                "PENDING"
        );

        orderRepository.save(
                order
        );


        return buildRazorpayOrderResponse(
                payment,
                order
        );
    }


    // =========================================================
    // RAZORPAY PAYMENT SIGNATURE VERIFICATION
    // =========================================================

    @Override
    @Transactional
    public void verifyRazorpayPayment(
            String authenticatedPhone,
            RazorpayVerifyRequest request
    ) {

        User user =
                userRepository
                        .findByPhone(
                                authenticatedPhone
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "User not found",
                                        404
                                )
                        );


        Payment payment =
                paymentRepository
                        .findByGatewayOrderIdForUpdate(
                                request.getRazorpayOrderId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Payment transaction not found",
                                        404
                                )
                        );


        // -----------------------------------------------------
        // OWNERSHIP
        // -----------------------------------------------------

        if (!payment.getUser()
                .getId()
                .equals(user.getId())) {

            throw new BusinessException(
                    "You are not allowed to verify this payment",
                    403
            );
        }


        // -----------------------------------------------------
        // IDEMPOTENCY
        // -----------------------------------------------------

        if (payment.getStatus()
                == PaymentStatus.SUCCESS) {

            /*
             * Already verified successfully.
             */
            return;
        }


        // -----------------------------------------------------
        // PAYMENT SIGNATURE
        // -----------------------------------------------------

        boolean valid =
                paymentGatewayClient
                        .verifyPaymentSignature(
                                payment.getGatewayOrderId(),
                                request.getRazorpayPaymentId(),
                                request.getRazorpaySignature()
                        );


        if (!valid) {

            throw new BusinessException(
                    "Invalid Razorpay payment signature",
                    400
            );
        }


        // -----------------------------------------------------
        // SAVE PAYMENT SUCCESS
        // -----------------------------------------------------

        payment.setGatewayPaymentId(
                request.getRazorpayPaymentId()
        );

        payment.setTransactionReference(
                request.getRazorpayPaymentId()
        );

        payment.setStatus(
                PaymentStatus.SUCCESS
        );

        payment.setPaidAt(
                OffsetDateTime.now()
        );

        paymentRepository.save(
                payment
        );


        // -----------------------------------------------------
        // LOCK ORDER
        // -----------------------------------------------------

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
         * IMPORTANT RACE CONDITION
         *
         * Customer could cancel order while
         * payment gateway was processing.
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


        orderRepository.save(
                order
        );
    }


    // =========================================================
    // REFUND
    // =========================================================

    @Override
    @Transactional
    public void initiateRefund(
            Long orderId,
            String reason
    ) {

        Order order =
                orderRepository
                        .findByIdForUpdate(
                                orderId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Order not found",
                                        404
                                )
                        );


        Payment payment =
                paymentRepository
                        .findFirstByOrderIdOrderByCreatedAtDesc(
                                orderId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Payment not found",
                                        404
                                )
                        );


        // -----------------------------------------------------
        // ONLY SUCCESS PAYMENT
        // -----------------------------------------------------

        if (payment.getStatus()
                != PaymentStatus.SUCCESS) {

            throw new BusinessException(
                    "Only successful payments can be refunded",
                    409
            );
        }


        // -----------------------------------------------------
        // GATEWAY PAYMENT REQUIRED
        // -----------------------------------------------------

        if (payment.getGatewayPaymentId() == null
                || payment.getGatewayPaymentId()
                        .isBlank()) {

            throw new BusinessException(
                    "Gateway payment ID is missing",
                    409
            );
        }


        // -----------------------------------------------------
        // EXISTING REFUND
        // -----------------------------------------------------

        PaymentRefund refund =
                paymentRefundRepository
                        .findByPaymentId(
                                payment.getId()
                        )
                        .orElse(null);


        if (refund != null) {

            if (refund.getStatus()
                    == RefundStatus.SUCCESS) {

                return;
            }

            if (refund.getStatus()
                    == RefundStatus.PROCESSING
                    &&
                    refund.getGatewayRefundId()
                            != null) {

                return;
            }
        }


        // -----------------------------------------------------
        // CREATE REFUND RECORD
        // -----------------------------------------------------

        if (refund == null) {

            refund =
                    new PaymentRefund();

            refund.setPayment(
                    payment
            );

            refund.setOrder(
                    order
            );

            refund.setRefundAmount(
                    payment.getAmount()
            );

            refund.setRefundReference(
                    "REF-"
                            + order.getOrderNumber()
            );

            refund.setReason(
                    reason
            );

            refund.setStatus(
                    RefundStatus.PROCESSING
            );

            refund =
                    paymentRefundRepository.save(
                            refund
                    );
        }


        // -----------------------------------------------------
        // CALL RAZORPAY
        // -----------------------------------------------------

        PaymentGatewayClient.GatewayRefund
                gatewayRefund;

        try {

            gatewayRefund =
                    paymentGatewayClient.createRefund(
                            payment.getGatewayPaymentId(),
                            payment.getAmount(),
                            order.getOrderNumber()
                    );

        } catch (RuntimeException ex) {

            refund.setStatus(
                    RefundStatus.FAILED
            );

            refund.setProcessedAt(
                    OffsetDateTime.now()
            );

            paymentRefundRepository.save(
                    refund
            );

            throw ex;
        }


        // -----------------------------------------------------
        // SAVE GATEWAY REFUND
        // -----------------------------------------------------

        refund.setGatewayRefundId(
                gatewayRefund.refundId()
        );

        refund.setStatus(
                RefundStatus.PROCESSING
        );

        paymentRefundRepository.save(
                refund
        );


        // -----------------------------------------------------
        // PAYMENT STATE
        // -----------------------------------------------------

        payment.setStatus(
                PaymentStatus.REFUND_PENDING
        );

        paymentRepository.save(
                payment
        );


        // -----------------------------------------------------
        // ORDER STATE
        // -----------------------------------------------------

        order.setPaymentStatus(
                "REFUND_PENDING"
        );

        orderRepository.save(
                order
        );
    }


    // =========================================================
    // LEGACY INTERNAL WEBHOOK
    // =========================================================

    @Override
    @Transactional
    public PaymentResponse processWebhook(
            PaymentWebhookRequest request
    ) {

        Payment payment =
                paymentRepository
                        .findByGatewayOrderIdForUpdate(
                                request.getGatewayOrderId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Payment not found for gateway order",
                                        404
                                )
                        );


        // -----------------------------------------------------
        // IDEMPOTENCY
        // -----------------------------------------------------

        if (payment.getStatus()
                == PaymentStatus.SUCCESS) {

            return mapToResponse(
                    payment
            );
        }


        PaymentStatus newStatus =
                request.getStatus();


        // =====================================================
        // SUCCESS
        // =====================================================

        if (newStatus
                == PaymentStatus.SUCCESS) {

            payment.setStatus(
                    PaymentStatus.SUCCESS
            );

            payment.setGatewayPaymentId(
                    request.getGatewayPaymentId()
            );

            payment.setTransactionReference(
                    request.getTransactionReference()
            );

            payment.setPaidAt(
                    OffsetDateTime.now()
            );


            Order order =
                    orderRepository
                            .findByIdForUpdate(
                                    payment
                                            .getOrder()
                                            .getId()
                            )
                            .orElseThrow(() ->
                                    new BusinessException(
                                            "Order not found",
                                            404
                                    )
                            );


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


            orderRepository.save(
                    order
            );
        }


        // =====================================================
        // FAILED
        // =====================================================

        else if (newStatus
                == PaymentStatus.FAILED) {

            payment.setStatus(
                    PaymentStatus.FAILED
            );

            payment.setGatewayPaymentId(
                    request.getGatewayPaymentId()
            );

            payment.setFailureReason(
                    request.getFailureReason()
            );


            Order order =
                    orderRepository
                            .findByIdForUpdate(
                                    payment
                                            .getOrder()
                                            .getId()
                            )
                            .orElseThrow(() ->
                                    new BusinessException(
                                            "Order not found",
                                            404
                                    )
                            );


            if (!"PAID".equalsIgnoreCase(
                    order.getPaymentStatus()
            )) {

                order.setPaymentStatus(
                        "FAILED"
                );

                orderRepository.save(
                        order
                );
            }
        }


        // =====================================================
        // OTHER STATUS
        // =====================================================

        else {

            payment.setStatus(
                    newStatus
            );
        }


        payment =
                paymentRepository.save(
                        payment
                );


        return mapToResponse(
                payment
        );
    }


    // =========================================================
    // RAZORPAY RESPONSE
    // =========================================================

    private RazorpayOrderResponse
    buildRazorpayOrderResponse(
            Payment payment,
            Order order
    ) {

        long amountInPaise =
                payment.getAmount()
                        .movePointRight(2)
                        .longValueExact();


        return RazorpayOrderResponse
                .builder()
                .paymentId(
                        payment.getId()
                )
                .orderId(
                        order.getId()
                )
                .orderNumber(
                        order.getOrderNumber()
                )
                .amount(
                        payment.getAmount()
                )
                .amountInPaise(
                        amountInPaise
                )
                .currency(
                        "INR"
                )
                .razorpayOrderId(
                        payment.getGatewayOrderId()
                )
                .razorpayKeyId(
                        razorpayKeyId
                )
                .paymentStatus(
                        payment.getStatus().name()
                )
                .build();
    }


    // =========================================================
    // TRANSACTION REFERENCE
    // =========================================================

    private String generateTransactionReference() {

        return "AASPAAS-TXN-"
                + UUID.randomUUID()
                        .toString()
                        .replace(
                                "-",
                                ""
                        )
                        .substring(
                                0,
                                20
                        )
                        .toUpperCase();
    }


    // =========================================================
    // RESPONSE MAPPER
    // =========================================================

    private PaymentResponse mapToResponse(
            Payment payment
    ) {

        return PaymentResponse
                .builder()
                .paymentId(
                        payment.getId()
                )
                .orderId(
                        payment
                                .getOrder()
                                .getId()
                )
                .orderNumber(
                        payment
                                .getOrder()
                                .getOrderNumber()
                )
                .amount(
                        payment.getAmount()
                )
                .paymentMethod(
                        payment.getPaymentMethod()
                )
                .status(
                        payment.getStatus()
                )
                .transactionReference(
                        payment.getTransactionReference()
                )
                .gatewayPaymentId(
                        payment.getGatewayPaymentId()
                )
                .gatewayOrderId(
                        payment.getGatewayOrderId()
                )
                .failureReason(
                        payment.getFailureReason()
                )
                .paidAt(
                        payment.getPaidAt()
                )
                .createdAt(
                        payment.getCreatedAt()
                )
                .build();
    }
}