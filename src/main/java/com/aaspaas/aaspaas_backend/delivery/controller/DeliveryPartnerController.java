package com.aaspaas.aaspaas_backend.delivery.controller;

import com.aaspaas.aaspaas_backend.delivery.dto.CreateDeliveryPartnerRequest;
import com.aaspaas.aaspaas_backend.delivery.dto.DeliveryPartnerResponse;
import com.aaspaas.aaspaas_backend.delivery.service.DeliveryPartnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/delivery/partners")
@RequiredArgsConstructor
public class DeliveryPartnerController {

    private final DeliveryPartnerService deliveryPartnerService;

    @PostMapping("/register")
    public ResponseEntity<DeliveryPartnerResponse> register(
            @RequestBody CreateDeliveryPartnerRequest request) {

        return ResponseEntity.ok(
                deliveryPartnerService.register(request)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<DeliveryPartnerResponse> getMyProfile() {

        return ResponseEntity.ok(
                deliveryPartnerService.getMyProfile()
        );
    }

    @PutMapping("/availability")
    public ResponseEntity<DeliveryPartnerResponse> updateAvailability(
            @RequestParam String status) {

        return ResponseEntity.ok(
                deliveryPartnerService.updateAvailability(status)
        );
    }
}