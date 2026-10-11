package com.aaspaas.aaspaas_backend.marketplace.controller;

import com.aaspaas.aaspaas_backend.common.dto.ApiResponse;
import com.aaspaas.aaspaas_backend.marketplace.dto.*;
import com.aaspaas.aaspaas_backend.marketplace.enums.BookingStatus;
import com.aaspaas.aaspaas_backend.marketplace.service.ServiceBookingService;
import com.aaspaas.aaspaas_backend.marketplace.service.ServiceListingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/** Covered by the existing /api/service-provider/** rule (SERVICE_PROVIDER, ADMIN). */
@RestController
@RequestMapping("/api/service-provider")
@RequiredArgsConstructor
public class ProviderServiceController {

    private final ServiceListingService listingService;
    private final ServiceBookingService bookingService;

    // ---- listings ----
    @PostMapping("/listings")
    public ResponseEntity<ApiResponse<ListingResponse>> create(
            @Valid @RequestBody ListingRequest request, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Listing submitted for approval", listingService.create(request, principal.getName())));
    }

    @PutMapping("/listings/{id}")
    public ResponseEntity<ApiResponse<ListingResponse>> update(
            @PathVariable Long id, @Valid @RequestBody ListingRequest request, Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "Listing updated", listingService.update(id, request, principal.getName())));
    }

    @PatchMapping("/listings/{id}/pause")
    public ResponseEntity<ApiResponse<ListingResponse>> pause(
            @PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "Listing paused", listingService.pause(id, principal.getName())));
    }

    @PatchMapping("/listings/{id}/resume")
    public ResponseEntity<ApiResponse<ListingResponse>> resume(
            @PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "Listing resumed", listingService.resume(id, principal.getName())));
    }

    @GetMapping("/listings")
    public ResponseEntity<ApiResponse<Page<ListingResponse>>> myListings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "Listings fetched", listingService.myListings(principal.getName(), page, size)));
    }

    // ---- bookings ----
    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> bookings(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "Bookings fetched", bookingService.providerBookings(principal.getName(), status, page, size)));
    }

    @PatchMapping("/bookings/{id}/accept")
    public ResponseEntity<ApiResponse<BookingResponse>> accept(
            @PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "Booking accepted", bookingService.accept(id, principal.getName())));
    }

    @PatchMapping("/bookings/{id}/reject")
    public ResponseEntity<ApiResponse<BookingResponse>> reject(
            @PathVariable Long id, @Valid @RequestBody ReasonRequest body, Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "Booking rejected", bookingService.reject(id, body.getReason(), principal.getName())));
    }

    @PatchMapping("/bookings/{id}/start")
    public ResponseEntity<ApiResponse<BookingResponse>> start(
            @PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "Booking started", bookingService.start(id, principal.getName())));
    }

    @PatchMapping("/bookings/{id}/complete")
    public ResponseEntity<ApiResponse<BookingResponse>> complete(
            @PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "Booking completed", bookingService.complete(id, principal.getName())));
    }

    @PatchMapping("/bookings/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancel(
            @PathVariable Long id, @Valid @RequestBody ReasonRequest body, Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "Booking cancelled", bookingService.cancelByProvider(id, body.getReason(), principal.getName())));
    }
}
