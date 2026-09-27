package com.aaspaas.aaspaas_backend.delivery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RejectDeliveryAssignmentRequest {

    @NotBlank(message = "Rejection reason is required")
    @Size(
            min = 3,
            max = 500,
            message = "Rejection reason must be between 3 and 500 characters"
    )
    private String reason;
}