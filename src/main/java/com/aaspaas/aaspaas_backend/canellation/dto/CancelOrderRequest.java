package com.aaspaas.aaspaas_backend.cancellation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelOrderRequest {

    @NotBlank(message = "Cancellation reason is required")
    @Size(
            min = 3,
            max = 500,
            message = "Cancellation reason must be between 3 and 500 characters"
    )
    private String reason;
}