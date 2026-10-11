package com.aaspaas.aaspaas_backend.marketplace.controller;

import com.aaspaas.aaspaas_backend.common.dto.ApiResponse;
import com.aaspaas.aaspaas_backend.marketplace.dto.BookingResponse;
import com.aaspaas.aaspaas_backend.marketplace.dto.CreateBookingRequest;
import com.aaspaas.aaspaas_backend.marketplace.dto.ReasonRequest;
import com.aaspaas.aaspaas_backend.marketplace.service.ServiceBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/service-bookings")
@RequiredArgsConstructor
public class CustomerBookingController {

    private final ServiceBookingService bookingService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> create(
            @Valid @RequestBody CreateBookingRequest request, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Booking requested", bookingService.create(request, principal.getName())));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> mine(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "Bookings fetched", bookingService.myBookings(principal.getName(), page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> get(
            @PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "Booking fetched", bookingService.getMine(id, principal.getName())));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancel(
            @PathVariable Long id, @Valid @RequestBody ReasonRequest body, Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "Booking cancelled", bookingService.cancelByCustomer(id, body.getReason(), principal.getName())));
    }
}
