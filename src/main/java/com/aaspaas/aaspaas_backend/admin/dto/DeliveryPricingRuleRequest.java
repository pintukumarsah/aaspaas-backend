package com.aaspaas.aaspaas_backend.admin.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
public class DeliveryPricingRuleRequest {

    @NotBlank(message = "Rule name is required")
    @Size(max = 100, message = "Rule name must not exceed 100 characters")
    private String ruleName;

    @NotBlank(message = "Delivery mode is required")
    @Size(max = 30, message = "Delivery mode must not exceed 30 characters")
    private String deliveryMode;

    @NotNull(message = "Base fee is required")
    @DecimalMin(value = "0.00", message = "Base fee cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Base fee supports up to 2 decimal places")
    private BigDecimal baseFee;

    @NotNull(message = "Per-kilometre fee is required")
    @DecimalMin(value = "0.00", message = "Per-kilometre fee cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Per-kilometre fee supports up to 2 decimal places")
    private BigDecimal perKmFee;

    @NotNull(message = "Minimum delivery fee is required")
    @DecimalMin(value = "0.00", message = "Minimum delivery fee cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Minimum fee supports up to 2 decimal places")
    private BigDecimal minimumDeliveryFee;

    @DecimalMin(value = "0.00", message = "Maximum delivery fee cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Maximum fee supports up to 2 decimal places")
    private BigDecimal maximumDeliveryFee;

    @NotNull(message = "Commission percentage is required")
    @DecimalMin(value = "0.00", message = "Commission percentage cannot be negative")
    @DecimalMax(value = "100.00", message = "Commission percentage cannot exceed 100")
    @Digits(integer = 3, fraction = 2, message = "Commission percentage supports up to 2 decimal places")
    private BigDecimal commissionPercentage;

    @NotNull(message = "Fixed platform fee is required")
    @DecimalMin(value = "0.00", message = "Fixed platform fee cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Fixed platform fee supports up to 2 decimal places")
    private BigDecimal fixedPlatformFee;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "[A-Za-z]{3}", message = "Currency must be a 3-letter alphabetic code, such as INR")
    private String currency = "INR";

    @NotNull(message = "Effective-from date/time is required")
    private OffsetDateTime effectiveFrom;

    private OffsetDateTime effectiveTo;

    private Boolean active = Boolean.TRUE;
}
