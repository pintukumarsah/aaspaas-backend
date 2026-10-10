package com.aaspaas.aaspaas_backend.rating.dto;

import com.aaspaas.aaspaas_backend.rating.enums.RatingTargetType;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRatingRequest {

    @NotNull(
            message = "Target type is required"
    )
    private RatingTargetType targetType;


    /**
     * Required when targetType = BUSINESS
     */
    private Long businessId;


    /**
     * Required when targetType = DELIVERY_PARTNER
     *
     * This is DeliveryPartner.id,
     * NOT User.id.
     */
    private Long deliveryPartnerId;


    /**
     * Required for every rating.
     */
    @NotNull(
            message = "Order ID is required"
    )
    private Long orderId;


    /**
     * Required when targetType = DELIVERY_PARTNER
     */
    private Long deliveryAssignmentId;


    @NotNull(
            message = "Rating is required"
    )
    @Min(
            value = 1,
            message = "Rating must be between 1 and 5"
    )
    @Max(
            value = 5,
            message = "Rating must be between 1 and 5"
    )
    private Short rating;


    @Size(
            max = 2000,
            message = "Review cannot exceed 2000 characters"
    )
    private String review;
}