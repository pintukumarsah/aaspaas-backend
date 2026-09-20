package com.aaspaas.aaspaas_backend.delivery.websocket;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record LiveLocationMessage(
        Long assignmentId,
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal accuracyMeters,
        String status,
        OffsetDateTime recordedAt
) {}