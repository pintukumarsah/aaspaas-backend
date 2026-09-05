package com.aaspaas.aaspaas_backend.business.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateBusinessRequest {

    @NotBlank(message = "Business name is required")
    @Size(
            min = 2,
            max = 150,
            message = "Business name must be between 2 and 150 characters"
    )
    private String name;

    @Size(
            max = 1000,
            message = "Description cannot exceed 1000 characters"
    )
    private String description;

    @NotBlank(message = "Business type is required")
    private String businessType;

    private String phone;

    private Long addressId;

    private BigDecimal  latitude;

    private BigDecimal  longitude;
}