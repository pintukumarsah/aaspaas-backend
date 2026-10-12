package com.aaspaas.aaspaas_backend.order.dispute.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DisputeRefundExecutionResponse {
    private Long disputeId;
    private Long orderId;
    private BigDecimal refundAmount;
    /** Refund was sent to the gateway. Final confirmation arrives later through the Razorpay webhook. */
    private String refundState;
}
