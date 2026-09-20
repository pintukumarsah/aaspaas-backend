package com.aaspaas.aaspaas_backend.delivery.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PartnerRouteUpdateRequest(

        @NotNull
        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        BigDecimal currentLatitude,

        @NotNull
        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        BigDecimal currentLongitude,

        @NotNull
        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        BigDecimal destinationLatitude,

        @NotNull
        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        BigDecimal destinationLongitude,

        @NotBlank
        String destinationName,

        OffsetDateTime plannedDepartureAt,

        Boolean routeAvailable
) {
}