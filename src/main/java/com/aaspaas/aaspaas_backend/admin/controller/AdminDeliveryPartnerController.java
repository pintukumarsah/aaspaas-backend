package com.aaspaas.aaspaas_backend.admin.controller;

import com.aaspaas.aaspaas_backend.admin.service.DeliveryPartnerModerationService;
import com.aaspaas.aaspaas_backend.common.dto.ApiResponse;
import com.aaspaas.aaspaas_backend.delivery.dto.DeliveryPartnerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/delivery-partners")
@RequiredArgsConstructor
public class AdminDeliveryPartnerController {

    private final DeliveryPartnerModerationService moderationService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<DeliveryPartnerResponse>>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                "Delivery partners retrieved successfully",
                moderationService.getPartners(status, page, size)));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<DeliveryPartnerResponse>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Delivery partner approved successfully", moderationService.approve(id)));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<DeliveryPartnerResponse>> reject(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Delivery partner rejected successfully", moderationService.reject(id)));
    }

    @PatchMapping("/{id}/suspend")
    public ResponseEntity<ApiResponse<DeliveryPartnerResponse>> suspend(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Delivery partner suspended successfully", moderationService.suspend(id)));
    }

    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<ApiResponse<DeliveryPartnerResponse>> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Delivery partner reactivated successfully", moderationService.reactivate(id)));
    }
}
