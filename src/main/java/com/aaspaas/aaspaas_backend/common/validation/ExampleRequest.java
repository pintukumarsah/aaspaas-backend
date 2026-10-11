package com.aaspaas.aaspaas_backend.common.validation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ExampleRequest(

        @NotBlank(message = "Name is required")
        @Size(
                min = 2,
                max = 100,
                message = "Name must contain between 2 and 100 characters"
        )
        String name

) {
}