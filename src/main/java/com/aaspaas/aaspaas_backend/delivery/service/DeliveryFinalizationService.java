package com.aaspaas.aaspaas_backend.delivery.service;

import com.aaspaas.aaspaas_backend.delivery.dto.DeliveryCompletionResponse;

public interface DeliveryFinalizationService {

    DeliveryCompletionResponse finalizeDelivery(
            Long assignmentId,
            Long finalizedBy
    );
}