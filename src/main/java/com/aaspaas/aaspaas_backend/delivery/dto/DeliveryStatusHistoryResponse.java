package com.aaspaas.aaspaas_backend.delivery.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Builder
public class DeliveryStatusHistoryResponse {

    private Long id;

    private Long assignmentId;

    private String status;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String remarks;

    private Long createdBy;

    private OffsetDateTime createdAt;
}