package com.aaspaas.aaspaas_backend.marketplace.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class CreateBookingRequest {

    @NotNull(message = "Listing is required")
    private Long listingId;

    @NotNull(message = "Scheduled time is required")
    private OffsetDateTime scheduledAt;

    @NotBlank(message = "Address is required")
    @Size(min = 10, max = 500)
    private String addressText;

    @Size(max = 1000)
    private String notes;

    /** Required (1-24) for HOURLY listings, ignored for FIXED. */
    @Min(1)
    @Max(24)
    private Integer estimatedHours;
}
