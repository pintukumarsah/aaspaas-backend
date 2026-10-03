package com.aaspaas.aaspaas_backend.commission.dto;


import com.aaspaas.aaspaas_backend.commission.enums.CommissionStatus;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Builder
public class CommissionResponse {

    private Long id;

    private Long orderId;

    private Long deliveryAssignmentId;

    private Long businessId;

    private Long partnerId;

    private String commissionType;

    private BigDecimal grossAmount;

    private BigDecimal commissionRate;

    private BigDecimal commissionAmount;

    private CommissionStatus status;

    private String description;

    private OffsetDateTime createdAt;
}