package com.aaspaas.aaspaas_backend.marketplace.controller;

import com.aaspaas.aaspaas_backend.common.dto.ApiResponse;
import com.aaspaas.aaspaas_backend.marketplace.dto.ListingResponse;
import com.aaspaas.aaspaas_backend.marketplace.service.ServiceListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Public (no JWT) browsing of ACTIVE service listings. */
@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class PublicServiceController {

    private final ServiceListingService listingService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ListingResponse>>> search(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String city,
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                "Services fetched", listingService.search(categoryId, city, query, page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ListingResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Service fetched", listingService.getPublic(id)));
    }
}
