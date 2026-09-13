package com.aaspaas.aaspaas_backend.delivery.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Builder
public class DeliveryTrackingResponse {

    private Long id;

    private Long assignmentId;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private BigDecimal accuracyMeters;

    private OffsetDateTime recordedAt;
}