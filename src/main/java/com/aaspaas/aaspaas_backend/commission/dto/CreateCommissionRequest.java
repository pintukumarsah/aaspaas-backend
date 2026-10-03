package com.aaspaas.aaspaas_backend.commission.dto;

import jakarta.validation.constraints.*;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateCommissionRequest {

    @NotNull
    @Positive
    private Long orderId;

    private Long deliveryAssignmentId;

    private Long businessId;

    private Long partnerId;

    @NotBlank
    @Size(max = 40)
    private String commissionType;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal grossAmount;

    @DecimalMin("0.00")
    @DecimalMax("100.00")
    private BigDecimal commissionRate;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal commissionAmount;

    @Size(max = 500)
    private String description;
}