package com.aaspaas.aaspaas_backend.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateProductRequest {

    @NotNull(message = "Business ID is required")
    private Long businessId;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotBlank(message = "Product name is required")
    @Size(
            min = 2,
            max = 200,
            message = "Product name must be between 2 and 200 characters"
    )
    private String name;

    @NotBlank(message = "Slug is required")
    @Size(max = 220)
    private String slug;

    @Size(max = 2000)
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(
            value = "0.01",
            message = "Price must be greater than 0"
    )
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @DecimalMin(
            value = "0.000",
            message = "Stock cannot be negative"
    )
    private BigDecimal stockQuantity;

    @Size(max = 30)
    private String unit;

    private Boolean available = true;
}