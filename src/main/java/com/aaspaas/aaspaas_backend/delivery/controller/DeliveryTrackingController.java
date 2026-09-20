package com.aaspaas.aaspaas_backend.delivery.controller;

import com.aaspaas.aaspaas_backend.common.dto.ApiResponse;
import com.aaspaas.aaspaas_backend.delivery.dto.*;
import com.aaspaas.aaspaas_backend.delivery.service.DeliveryTrackingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
public class DeliveryTrackingController {

    private final DeliveryTrackingService trackingService;

    @PostMapping("/assignments/{assignmentId}/tracking")
    public ResponseEntity<ApiResponse<DeliveryTrackingResponse>> addLocation(
            @PathVariable Long assignmentId,
            @Valid @RequestBody CreateDeliveryTrackingRequest request) {

        return ResponseEntity.ok(new ApiResponse<>(true, "Location recorded successfully",
                trackingService.addLocation(assignmentId, request)));
    }

    @GetMapping("/assignments/{assignmentId}/tracking/latest")
    public ResponseEntity<ApiResponse<DeliveryTrackingResponse>> getLatestLocation(
            @PathVariable Long assignmentId) {

        return ResponseEntity.ok(new ApiResponse<>(true, "Latest location fetched",
                trackingService.getLatestLocation(assignmentId)));
    }

    @GetMapping("/assignments/{assignmentId}/tracking")
    public ResponseEntity<ApiResponse<List<DeliveryTrackingResponse>>> getTrackingHistory(
            @PathVariable Long assignmentId) {

        return ResponseEntity.ok(new ApiResponse<>(true, "Tracking history fetched successfully",
                trackingService.getTrackingHistory(assignmentId)));
    }

    @PutMapping("/assignments/{assignmentId}/status")
    public ResponseEntity<ApiResponse<DeliveryStatusHistoryResponse>> updateStatus(
            @PathVariable Long assignmentId,
            @Valid @RequestBody UpdateDeliveryStatusRequest request) {

        return ResponseEntity.ok(new ApiResponse<>(true, "Status updated successfully",
                trackingService.updateStatus(assignmentId, request)));
    }

    @GetMapping("/assignments/{assignmentId}/status-history")
    public ResponseEntity<ApiResponse<List<DeliveryStatusHistoryResponse>>> getStatusHistory(
            @PathVariable Long assignmentId) {

        return ResponseEntity.ok(new ApiResponse<>(true, "Status history fetched successfully",
                trackingService.getStatusHistory(assignmentId)));
    }

    @PostMapping("/assignments/{assignmentId}/live-location")
    public ResponseEntity<ApiResponse<DeliveryTrackingResponse>> updateLiveLocation(
            @PathVariable Long assignmentId,
            @Valid @RequestBody LocationUpdateRequest request) {

        return ResponseEntity.ok(new ApiResponse<>(true, "Location updated successfully",
                trackingService.updateLocation(assignmentId, request)));
    }
}