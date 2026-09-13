package com.aaspaas.aaspaas_backend.delivery.controller;

import com.aaspaas.aaspaas_backend.delivery.dto.DeliveryAssignmentResponse;
import com.aaspaas.aaspaas_backend.delivery.dto.DeliveryOtpResponse;
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

    @PostMapping("/{assignmentId}/accept")
    public ResponseEntity<DeliveryAssignmentResponse>
    acceptAssignment(
            @PathVariable Long assignmentId) {

        return ResponseEntity.ok(
                deliveryAssignmentService
                        .acceptAssignment(
                                assignmentId
                        )
        );
    }

    @PostMapping("/{assignmentId}/pickup-otp")
    public ResponseEntity<DeliveryOtpResponse>
    generatePickupOtp(
            @PathVariable Long assignmentId) {

        return ResponseEntity.ok(
                deliveryAssignmentService
                        .generatePickupOtp(
                                assignmentId
                        )
        );
    }

    @PostMapping("/{assignmentId}/pickup-otp/verify")
    public ResponseEntity<DeliveryAssignmentResponse>
    verifyPickupOtp(
            @PathVariable Long assignmentId,
            @RequestBody
            com.aaspaas.aaspaas_backend.delivery.dto.VerifyDeliveryOtpRequest request) {

        return ResponseEntity.ok(
                deliveryAssignmentService
                        .verifyPickupOtp(
                                assignmentId,
                                request.getOtp()
                        )
        );
    }

    @PostMapping("/{assignmentId}/delivery-otp")
    public ResponseEntity<DeliveryOtpResponse>
    generateDeliveryOtp(
            @PathVariable Long assignmentId) {

        return ResponseEntity.ok(
                deliveryAssignmentService
                        .generateDeliveryOtp(
                                assignmentId
                        )
        );
    }

    @PostMapping("/{assignmentId}/delivery-otp/verify")
    public ResponseEntity<DeliveryAssignmentResponse>
    verifyDeliveryOtp(
            @PathVariable Long assignmentId,
            @RequestBody
            com.aaspaas.aaspaas_backend.delivery.dto.VerifyDeliveryOtpRequest request) {

        return ResponseEntity.ok(
                deliveryAssignmentService
                        .verifyDeliveryOtp(
                                assignmentId,
                                request.getOtp()
                        )
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
                        .getAssignment(
                                assignmentId
                        )
        );
    }
}