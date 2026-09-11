package com.aaspaas.aaspaas_backend.delivery.service;

import com.aaspaas.aaspaas_backend.delivery.dto.CreateDeliveryRequest;
import com.aaspaas.aaspaas_backend.delivery.dto.DeliveryRequestResponse;

import java.util.List;

public interface DeliveryRequestService {

    DeliveryRequestResponse createRequest(
            CreateDeliveryRequest request,
            String authenticatedPhone
    );

    DeliveryRequestResponse getRequest(
            Long requestId,
            String authenticatedPhone
    );

    List<DeliveryRequestResponse> getMyRequests(
            String authenticatedPhone
    );

    void cancelRequest(
            Long requestId,
            String authenticatedPhone
    );
}