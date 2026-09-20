package com.aaspaas.aaspaas_backend.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
public class LiveLocationMessage {

    private Long assignmentId;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private BigDecimal accuracyMeters;

    private String status;

    private OffsetDateTime recordedAt;
}