package com.aaspaas.aaspaas_backend.marketplace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReasonRequest {

    @NotBlank(message = "Reason is required")
    @Size(max = 500)
    private String reason;
}
