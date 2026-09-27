package com.aaspaas.aaspaas_backend.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
@AllArgsConstructor
public class DeliveryCompletionResponse {

    private Long assignmentId;

    private Long orderId;

    private Long partnerId;

    private String assignmentStatus;

    private String orderStatus;

    private String partnerAvailabilityStatus;

    private String completionStatus;

    private OffsetDateTime completedAt;

    private boolean alreadyFinalized;
}