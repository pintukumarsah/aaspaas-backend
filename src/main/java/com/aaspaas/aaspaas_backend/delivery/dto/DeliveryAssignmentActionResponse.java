package com.aaspaas.aaspaas_backend.delivery.dto;

import com.aaspaas.aaspaas_backend.delivery.enums.DeliveryAssignmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
@AllArgsConstructor
public class DeliveryAssignmentActionResponse {

    private Long assignmentId;

    private DeliveryAssignmentStatus previousStatus;

    private DeliveryAssignmentStatus currentStatus;

    private Long partnerId;

    private Long deliveryRequestId;

    private Long orderId;

    private OffsetDateTime actionAt;

    private String message;
}