package com.aaspaas.aaspaas_backend.admin.controller;

import com.aaspaas.aaspaas_backend.admin.service.BusinessModerationService;
import com.aaspaas.aaspaas_backend.business.dto.BusinessResponse;
import com.aaspaas.aaspaas_backend.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final BusinessModerationService businessModerationService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, String>> dashboard() {
        return ResponseEntity.ok(
                Map.of("message", "Welcome to Admin Dashboard")
        );
    }

    @GetMapping("/businesses")
    public ResponseEntity<ApiResponse<Page<BusinessResponse>>>
    getBusinesses(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<BusinessResponse> result =
                businessModerationService.getBusinesses(
                        status, page, size
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Businesses fetched successfully",
                        result
                )
        );
    }

    @PatchMapping("/businesses/{businessId}/approve")
    public ResponseEntity<ApiResponse<BusinessResponse>>
    approveBusiness(@PathVariable Long businessId) {
        BusinessResponse result =
                businessModerationService.approveBusiness(businessId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Business approved successfully",
                        result
                )
        );
    }

    @PatchMapping("/businesses/{businessId}/reject")
    public ResponseEntity<ApiResponse<BusinessResponse>>
    rejectBusiness(@PathVariable Long businessId) {
        BusinessResponse result =
                businessModerationService.rejectBusiness(businessId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Business rejected successfully",
                        result
                )
        );
    }

    @PatchMapping("/businesses/{businessId}/suspend")
    public ResponseEntity<ApiResponse<BusinessResponse>>
    suspendBusiness(@PathVariable Long businessId) {
        BusinessResponse result =
                businessModerationService.suspendBusiness(businessId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Business suspended successfully",
                        result
                )
        );
    }

    @PatchMapping("/businesses/{businessId}/reactivate")
    public ResponseEntity<ApiResponse<BusinessResponse>>
    reactivateBusiness(@PathVariable Long businessId) {
        BusinessResponse result =
                businessModerationService.reactivateBusiness(businessId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Business reactivated successfully",
                        result
                )
        );
    }
}