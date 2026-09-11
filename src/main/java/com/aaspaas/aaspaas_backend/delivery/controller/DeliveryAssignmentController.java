package com.aaspaas.aaspaas_backend.delivery.controller;

import com.aaspaas.aaspaas_backend.delivery.dto.DeliveryAssignmentResponse;
import com.aaspaas.aaspaas_backend.delivery.service.DeliveryAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/delivery/assignments")
@RequiredArgsConstructor
public class DeliveryAssignmentController {

    private final DeliveryAssignmentService
            deliveryAssignmentService;

    @PostMapping("/accept-quote/{quoteId}")
    public ResponseEntity<DeliveryAssignmentResponse>
    acceptQuote(
            @PathVariable Long quoteId) {

        return ResponseEntity.ok(
                deliveryAssignmentService
                        .acceptQuote(quoteId)
        );
    }

    @GetMapping("/my")
    public ResponseEntity<DeliveryAssignmentResponse>
    getMyAssignment() {

        return ResponseEntity.ok(
                deliveryAssignmentService
                        .getMyAssignment()
        );
    }

    @GetMapping("/{assignmentId}")
    public ResponseEntity<DeliveryAssignmentResponse>
    getAssignment(
            @PathVariable Long assignmentId) {

        return ResponseEntity.ok(
                deliveryAssignmentService
                        .getAssignment(assignmentId)
        );
    }
}