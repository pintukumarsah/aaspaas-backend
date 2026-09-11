package com.aaspaas.aaspaas_backend.delivery.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Builder
public class DeliveryAssignmentResponse {

    private Long id;

    private Long deliveryRequestId;

    private Long quoteId;

    private Long partnerId;

    private Long partnerUserId;

    private String partnerName;

    private BigDecimal deliveryAmount;

    private Integer estimatedMinutes;

    private String status;

    private OffsetDateTime assignedAt;

    private OffsetDateTime acceptedAt;

    private OffsetDateTime pickedUpAt;

    private OffsetDateTime deliveredAt;
}