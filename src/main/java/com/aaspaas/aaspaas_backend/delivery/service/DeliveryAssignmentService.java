package com.aaspaas.aaspaas_backend.delivery.service;

import com.aaspaas.aaspaas_backend.delivery.dto.DeliveryAssignmentResponse;

public interface DeliveryAssignmentService {

    DeliveryAssignmentResponse acceptQuote(Long quoteId);

    DeliveryAssignmentResponse getMyAssignment();

    DeliveryAssignmentResponse getAssignment(
            Long assignmentId
    );
}