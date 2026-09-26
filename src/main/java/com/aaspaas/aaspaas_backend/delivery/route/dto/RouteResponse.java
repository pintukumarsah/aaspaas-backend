package com.aaspaas.aaspaas_backend.delivery.route.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class RouteResponse {

    private BigDecimal straightLineDistanceKm;

    private BigDecimal roadDistanceKm;

    private Integer estimatedDurationMinutes;

    private String provider;

    private String status;
}