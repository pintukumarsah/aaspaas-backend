package com.aaspaas.aaspaas_backend.marketplace.controller;

import com.aaspaas.aaspaas_backend.common.dto.ApiResponse;
import com.aaspaas.aaspaas_backend.marketplace.dto.ListingResponse;
import com.aaspaas.aaspaas_backend.marketplace.enums.ListingStatus;
import com.aaspaas.aaspaas_backend.marketplace.service.ServiceListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Covered by the existing /api/admin/** ADMIN rule. */
@RestController
@RequestMapping("/api/admin/services")
@RequiredArgsConstructor
public class AdminServiceController {

    private final ServiceListingService listingService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ListingResponse>>> list(
            @RequestParam(required = false) ListingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                "Services fetched", listingService.adminList(status, page, size)));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<ListingResponse>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Service approved", listingService.approve(id)));
    }

    @PatchMapping("/{id}/suspend")
    public ResponseEntity<ApiResponse<ListingResponse>> suspend(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Service suspended", listingService.suspend(id)));
    }

    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<ApiResponse<ListingResponse>> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Service reactivated", listingService.reactivate(id)));
    }
}
