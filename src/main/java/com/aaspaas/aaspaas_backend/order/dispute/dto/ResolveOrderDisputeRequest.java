package com.aaspaas.aaspaas_backend.order.dispute.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ResolveOrderDisputeRequest {
    @NotBlank(message = "resolutionCode is required")
    @Size(max = 40, message = "resolutionCode must not exceed 40 characters")
    private String resolutionCode;

    @NotBlank(message = "resolutionNotes is required")
    @Size(min = 10, max = 2000, message = "resolutionNotes must be between 10 and 2000 characters")
    private String resolutionNotes;

    @DecimalMin(value = "0.00", message = "approvedRefundAmount cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "approvedRefundAmount supports up to 2 decimal places")
    private BigDecimal approvedRefundAmount;
}
