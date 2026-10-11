package com.aaspaas.aaspaas_backend.marketplace.dto;

import com.aaspaas.aaspaas_backend.marketplace.enums.PricingType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ListingRequest {

    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 150)
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 20, max = 4000)
    private String description;

    @NotNull(message = "Pricing type is required")
    private PricingType pricingType;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "1.00", message = "Base price must be at least 1")
    @DecimalMax(value = "1000000.00")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal basePrice;

    @NotBlank(message = "City is required")
    @Size(max = 100)
    private String city;

    @Size(max = 150)
    private String locality;

    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private BigDecimal latitude;

    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private BigDecimal longitude;
}
