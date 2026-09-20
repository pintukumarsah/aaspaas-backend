package com.aaspaas.aaspaas_backend.delivery.controller;

import com.aaspaas.aaspaas_backend.common.dto.ApiResponse;
import com.aaspaas.aaspaas_backend.delivery.dto.DeliveryAssignmentResponse;
import com.aaspaas.aaspaas_backend.delivery.dto.DeliveryStatusHistoryResponse;
import com.aaspaas.aaspaas_backend.delivery.dto.OtpResponse;
import com.aaspaas.aaspaas_backend.delivery.dto.VerifyOtpRequest;
import com.aaspaas.aaspaas_backend.delivery.service.DeliveryAssignmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery/assignments")
@RequiredArgsConstructor
public class DeliveryAssignmentController {

    private final DeliveryAssignmentService assignmentService;

    @PostMapping("/accept-quote/{quoteId}")
    public ResponseEntity<ApiResponse<DeliveryAssignmentResponse>> acceptQuote(
            @PathVariable Long quoteId) {

        return ResponseEntity.ok(new ApiResponse<>(true, "Quote accepted",
                assignmentService.acceptQuote(quoteId)));
    }

    @PostMapping("/{assignmentId}/accept")
    public ResponseEntity<ApiResponse<DeliveryAssignmentResponse>> acceptAssignment(
            @PathVariable Long assignmentId) {

        return ResponseEntity.ok(new ApiResponse<>(true, "Assignment accepted",
                assignmentService.acceptAssignment(assignmentId)));
    }

    @PostMapping("/{assignmentId}/reject")
    public ResponseEntity<ApiResponse<DeliveryAssignmentResponse>> rejectAssignment(
            @PathVariable Long assignmentId) {

        return ResponseEntity.ok(new ApiResponse<>(true, "Delivery assignment rejected",
                assignmentService.rejectAssignment(assignmentId)));
    }

    @PostMapping("/{assignmentId}/cancel")
    public ResponseEntity<ApiResponse<DeliveryAssignmentResponse>> cancelAssignment(
            @PathVariable Long assignmentId) {

        return ResponseEntity.ok(new ApiResponse<>(true, "Delivery assignment cancelled",
                assignmentService.cancelAssignment(assignmentId)));
    }

    @PostMapping("/{assignmentId}/pickup-otp")
    public ResponseEntity<ApiResponse<OtpResponse>> generatePickupOtp(
            @PathVariable Long assignmentId) {

        return ResponseEntity.ok(new ApiResponse<>(true, "Pickup OTP generated",
                assignmentService.generatePickupOtp(assignmentId)));
    }

    @PostMapping("/{assignmentId}/pickup-otp/verify")
    public ResponseEntity<ApiResponse<DeliveryAssignmentResponse>> verifyPickupOtp(
            @PathVariable Long assignmentId,
            @Valid @RequestBody VerifyOtpRequest request) {

        return ResponseEntity.ok(new ApiResponse<>(true, "Pickup verified successfully",
                assignmentService.verifyPickupOtp(assignmentId, request.getOtp())));
    }

    @PostMapping("/{assignmentId}/out-for-delivery")
    public ResponseEntity<ApiResponse<DeliveryAssignmentResponse>> startDelivery(
            @PathVariable Long assignmentId) {

        return ResponseEntity.ok(new ApiResponse<>(true, "Order is out for delivery",
                assignmentService.startDelivery(assignmentId)));
    }

    @PostMapping("/{assignmentId}/delivery-otp")
    public ResponseEntity<ApiResponse<OtpResponse>> generateDeliveryOtp(
            @PathVariable Long assignmentId) {

        return ResponseEntity.ok(new ApiResponse<>(true, "Delivery OTP generated",
                assignmentService.generateDeliveryOtp(assignmentId)));
    }

    @PostMapping("/{assignmentId}/delivery-otp/verify")
    public ResponseEntity<ApiResponse<DeliveryAssignmentResponse>> verifyDeliveryOtp(
            @PathVariable Long assignmentId,
            @Valid @RequestBody VerifyOtpRequest request) {

        return ResponseEntity.ok(new ApiResponse<>(true, "Order delivered successfully",
                assignmentService.verifyDeliveryOtp(assignmentId, request.getOtp())));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<DeliveryAssignmentResponse>> getMyAssignment() {

        return ResponseEntity.ok(new ApiResponse<>(true, "Assignment fetched",
                assignmentService.getMyAssignment()));
    }

    @GetMapping("/{assignmentId}")
    public ResponseEntity<ApiResponse<DeliveryAssignmentResponse>> getAssignment(
            @PathVariable Long assignmentId) {

        return ResponseEntity.ok(new ApiResponse<>(true, "Assignment fetched",
                assignmentService.getAssignment(assignmentId)));
    }

    @GetMapping("/{assignmentId}/history")
    public ResponseEntity<ApiResponse<List<DeliveryStatusHistoryResponse>>> getHistory(
            @PathVariable Long assignmentId) {

        return ResponseEntity.ok(new ApiResponse<>(true, "Delivery history fetched successfully",
                assignmentService.getHistory(assignmentId)));
    }
}