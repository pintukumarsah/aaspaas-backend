package com.aaspaas.aaspaas_backend.delivery.controller;

import com.aaspaas.aaspaas_backend.delivery.dto.*;
import com.aaspaas.aaspaas_backend.delivery.service.DeliveryTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
public class DeliveryTrackingController {

    private final DeliveryTrackingService trackingService;

    @PostMapping(
            "/assignments/{assignmentId}/tracking"
    )
    public ResponseEntity<DeliveryTrackingResponse>
    addLocation(
            @PathVariable Long assignmentId,
            @RequestBody
            CreateDeliveryTrackingRequest request) {

        return ResponseEntity.ok(
                trackingService.addLocation(
                        assignmentId,
                        request
                )
        );
    }

    @GetMapping(
            "/assignments/{assignmentId}/tracking/latest"
    )
    public ResponseEntity<DeliveryTrackingResponse>
    getLatestLocation(
            @PathVariable Long assignmentId) {

        return ResponseEntity.ok(
                trackingService.getLatestLocation(
                        assignmentId
                )
        );
    }

    @GetMapping(
            "/assignments/{assignmentId}/tracking"
    )
    public ResponseEntity<
            List<DeliveryTrackingResponse>>
    getTrackingHistory(
            @PathVariable Long assignmentId) {

        return ResponseEntity.ok(
                trackingService.getTrackingHistory(
                        assignmentId
                )
        );
    }

    @PutMapping(
            "/assignments/{assignmentId}/status"
    )
    public ResponseEntity<
            DeliveryStatusHistoryResponse>
    updateStatus(
            @PathVariable Long assignmentId,
            @RequestBody
            UpdateDeliveryStatusRequest request) {

        return ResponseEntity.ok(
                trackingService.updateStatus(
                        assignmentId,
                        request
                )
        );
    }

    @GetMapping(
            "/assignments/{assignmentId}/status-history"
    )
    public ResponseEntity<
            List<DeliveryStatusHistoryResponse>>
    getStatusHistory(
            @PathVariable Long assignmentId) {

        return ResponseEntity.ok(
                trackingService.getStatusHistory(
                        assignmentId
                )
        );
    }
}