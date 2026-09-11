package com.aaspaas.aaspaas_backend.product.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddProductImageRequest {

    @NotBlank(message = "Image URL is required")
    @Size(max = 2000, message = "Image URL must not exceed 2000 characters")
    private String imageUrl;

    @Min(value = 0, message = "Display order cannot be negative")
    @Max(value = 100, message = "Display order cannot exceed 100")
    private Integer displayOrder = 0;
}