package com.aaspaas.aaspaas_backend.rating.dto;

import com.aaspaas.aaspaas_backend.rating.enums.RatingStatus;
import com.aaspaas.aaspaas_backend.rating.enums.RatingTargetType;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class RatingResponse {

    private Long id;

    private Long reviewerId;

    private String reviewerName;

    private Long orderId;

    private RatingTargetType targetType;

    private Long businessId;

    private String businessName;

    private Long deliveryPartnerId;

    private Long deliveryAssignmentId;

    private Short rating;

    private String review;

    private RatingStatus status;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}