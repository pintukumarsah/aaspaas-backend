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
public class CreateOrderDisputeRequest {
    @NotBlank(message = "reasonCode is required")
    @Size(max = 40, message = "reasonCode must not exceed 40 characters")
    private String reasonCode;

    @NotBlank(message = "description is required")
    @Size(min = 10, max = 2000, message = "description must be between 10 and 2000 characters")
    private String description;

    @DecimalMin(value = "0.01", message = "requestedRefundAmount must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "requestedRefundAmount supports up to 2 decimal places")
    private BigDecimal requestedRefundAmount;
}
