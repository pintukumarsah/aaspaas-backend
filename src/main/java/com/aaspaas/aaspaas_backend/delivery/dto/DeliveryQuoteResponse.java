package com.aaspaas.aaspaas_backend.delivery.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Builder
public class DeliveryQuoteResponse {

    private Long id;

    private Long deliveryRequestId;

    private Long partnerId;

    private Long userId;

    private String partnerName;

    private BigDecimal quotedAmount;

    private Integer estimatedMinutes;

    private String message;

    private String status;

    private OffsetDateTime createdAt;
}