package com.aaspaas.aaspaas_backend.infra.locationcache;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record LocationPoint(
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal accuracyMeters,
        OffsetDateTime recordedAt) {
}
