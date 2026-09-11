package com.aaspaas.aaspaas_backend.delivery.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
public class DeliveryRequestResponse {

    private Long id;

    private Long orderId;

    private String orderNumber;

    private Long customerId;

    private Long pickupAddressId;

    private Long deliveryAddressId;

    private BigDecimal maxBudget;

    private String status;

    private String deliveryMode;

    private BigDecimal pickupLatitude;

    private BigDecimal pickupLongitude;

    private BigDecimal deliveryLatitude;

    private BigDecimal deliveryLongitude;

    private OffsetDateTime requestedDepartureAt;

    private OffsetDateTime requiredByAt;

    private BigDecimal searchRadiusKm;

    private String customerNote;

    private OffsetDateTime requestedAt;

    private OffsetDateTime expiresAt;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}