package com.aaspaas.aaspaas_backend.cancellation.service;

import com.aaspaas.aaspaas_backend.cancellation.dto.CancelOrderRequest;
import com.aaspaas.aaspaas_backend.cancellation.dto.CancellationResponse;

public interface OrderCancellationService {

    CancellationResponse cancelOrder(
            Long orderId,
            CancelOrderRequest request,
            String authenticatedPhone
    );
}