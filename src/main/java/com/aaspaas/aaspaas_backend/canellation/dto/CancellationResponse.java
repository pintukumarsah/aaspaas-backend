package com.aaspaas.aaspaas_backend.cancellation.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Builder
public class CancellationResponse {

    private Long orderId;

    private String orderNumber;

    private String orderStatus;

    private String paymentStatus;

    private boolean deliveryCancelled;

    private boolean refundCreated;

    private BigDecimal refundAmount;

    private String refundStatus;

    private String cancelledByType;

    private String reason;

    private OffsetDateTime cancelledAt;
}