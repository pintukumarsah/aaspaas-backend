package com.aaspaas.aaspaas_backend.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Builder
@AllArgsConstructor
public class DeliveryPartnerMatchResponse {

    private Long deliveryPartnerId;

    private Long userId;

    private String partnerName;

    private BigDecimal distanceFromPickupKm;

    private BigDecimal distanceFromDestinationKm;

    private String destinationName;

    private OffsetDateTime plannedDepartureAt;

    private Integer estimatedMinutes;

    private String matchType;

    private Boolean routeAvailable;
}