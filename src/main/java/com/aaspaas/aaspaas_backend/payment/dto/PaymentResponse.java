package com.aaspaas.aaspaas_backend.payment.dto;

import com.aaspaas.aaspaas_backend.payment.enums.PaymentMethod;
import com.aaspaas.aaspaas_backend.payment.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class PaymentResponse {

    private final Long paymentId;
    private final Long orderId;
    private final String orderNumber;
    private final BigDecimal amount;
    private final PaymentMethod paymentMethod;
    private final PaymentStatus status;
    private final String transactionReference;
    private final String gatewayPaymentId;
    private final String gatewayOrderId;
    private final String failureReason;
    private final OffsetDateTime paidAt;
    private final OffsetDateTime createdAt;

    private PaymentResponse(Builder b) {
        this.paymentId = b.paymentId;
        this.orderId = b.orderId;
        this.orderNumber = b.orderNumber;
        this.amount = b.amount;
        this.paymentMethod = b.paymentMethod;
        this.status = b.status;
        this.transactionReference = b.transactionReference;
        this.gatewayPaymentId = b.gatewayPaymentId;
        this.gatewayOrderId = b.gatewayOrderId;
        this.failureReason = b.failureReason;
        this.paidAt = b.paidAt;
        this.createdAt = b.createdAt;
    }

    public Long getPaymentId() { return paymentId; }
    public Long getOrderId() { return orderId; }
    public String getOrderNumber() { return orderNumber; }
    public BigDecimal getAmount() { return amount; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public PaymentStatus getStatus() { return status; }
    public String getTransactionReference() { return transactionReference; }
    public String getGatewayPaymentId() { return gatewayPaymentId; }
    public String getGatewayOrderId() { return gatewayOrderId; }
    public String getFailureReason() { return failureReason; }
    public OffsetDateTime getPaidAt() { return paidAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long paymentId;
        private Long orderId;
        private String orderNumber;
        private BigDecimal amount;
        private PaymentMethod paymentMethod;
        private PaymentStatus status;
        private String transactionReference;
        private String gatewayPaymentId;
        private String gatewayOrderId;
        private String failureReason;
        private OffsetDateTime paidAt;
        private OffsetDateTime createdAt;

        public Builder paymentId(Long v) { this.paymentId = v; return this; }
        public Builder orderId(Long v) { this.orderId = v; return this; }
        public Builder orderNumber(String v) { this.orderNumber = v; return this; }
        public Builder amount(BigDecimal v) { this.amount = v; return this; }
        public Builder paymentMethod(PaymentMethod v) { this.paymentMethod = v; return this; }
        public Builder status(PaymentStatus v) { this.status = v; return this; }
        public Builder transactionReference(String v) { this.transactionReference = v; return this; }
        public Builder gatewayPaymentId(String v) { this.gatewayPaymentId = v; return this; }
        public Builder gatewayOrderId(String v) { this.gatewayOrderId = v; return this; }
        public Builder failureReason(String v) { this.failureReason = v; return this; }
        public Builder paidAt(OffsetDateTime v) { this.paidAt = v; return this; }
        public Builder createdAt(OffsetDateTime v) { this.createdAt = v; return this; }
        public PaymentResponse build() { return new PaymentResponse(this); }
    }
}
