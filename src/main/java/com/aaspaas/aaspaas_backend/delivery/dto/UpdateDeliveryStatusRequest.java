package com.aaspaas.aaspaas_backend.delivery.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateDeliveryStatusRequest {

    @NotNull(message = "Status is required")
    private String status;

    private BigDecimal latitude;
    private BigDecimal longitude;
    private String remarks;
}