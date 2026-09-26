package com.aaspaas.aaspaas_backend.delivery.route.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RouteRequest {

    @NotNull
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private BigDecimal pickupLatitude;

    @NotNull
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private BigDecimal pickupLongitude;

    @NotNull
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private BigDecimal deliveryLatitude;

    @NotNull
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private BigDecimal deliveryLongitude;
}