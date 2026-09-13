package com.aaspaas.aaspaas_backend.delivery.service;

import com.aaspaas.aaspaas_backend.delivery.dto.*;

import java.util.List;

public interface DeliveryTrackingService {

    DeliveryTrackingResponse addLocation(
            Long assignmentId,
            CreateDeliveryTrackingRequest request
    );

    DeliveryTrackingResponse getLatestLocation(
            Long assignmentId
    );

    List<DeliveryTrackingResponse> getTrackingHistory(
            Long assignmentId
    );

    DeliveryStatusHistoryResponse updateStatus(
            Long assignmentId,
            UpdateDeliveryStatusRequest request
    );

    List<DeliveryStatusHistoryResponse> getStatusHistory(
            Long assignmentId
    );
}