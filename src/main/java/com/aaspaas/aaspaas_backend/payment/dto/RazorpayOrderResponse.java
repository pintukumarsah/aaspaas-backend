package com.aaspaas.aaspaas_backend.payment.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class RazorpayOrderResponse {

    private Long paymentId;

    private Long orderId;

    private String orderNumber;

    private BigDecimal amount;

    private long amountInPaise;

    private String currency;

    private String razorpayOrderId;

    private String razorpayKeyId;

    private String paymentStatus;
}