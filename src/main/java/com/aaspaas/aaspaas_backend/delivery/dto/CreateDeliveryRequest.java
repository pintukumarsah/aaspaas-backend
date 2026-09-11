package com.aaspaas.aaspaas_backend.delivery.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
public class CreateDeliveryRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Pickup address ID is required")
    private Long pickupAddressId;

    @NotNull(message = "Delivery address ID is required")
    private Long deliveryAddressId;

    @NotNull(message = "Maximum delivery budget is required")
    @DecimalMin(
        value = "0.00",
        message = "Budget cannot be negative"
    )
    private BigDecimal maxBudget;

    @Size(max = 30)
    private String deliveryMode = "DIRECT";

    private BigDecimal pickupLatitude;

    private BigDecimal pickupLongitude;

    private BigDecimal deliveryLatitude;

    private BigDecimal deliveryLongitude;

    private OffsetDateTime requestedDepartureAt;

    private OffsetDateTime requiredByAt;

    @DecimalMin(
        value = "0.1",
        message = "Search radius must be greater than zero"
    )
    private BigDecimal searchRadiusKm = BigDecimal.TEN;

    @Size(
        max = 500,
        message = "Customer note cannot exceed 500 characters"
    )
    private String customerNote;
}