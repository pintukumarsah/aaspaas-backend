package com.aaspaas.aaspaas_backend.delivery.controller;

import com.aaspaas.aaspaas_backend.delivery.dto.DeliveryPartnerMatchResponse;
import com.aaspaas.aaspaas_backend.delivery.service.DeliveryPartnerMatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/delivery/matching")
@RequiredArgsConstructor
public class DeliveryPartnerMatchingController {

    private final DeliveryPartnerMatchingService matchingService;

    @GetMapping("/partners")
    public ResponseEntity<List<DeliveryPartnerMatchResponse>> findMatches(

            @RequestParam BigDecimal pickupLatitude,

            @RequestParam BigDecimal pickupLongitude,

            @RequestParam BigDecimal destinationLatitude,

            @RequestParam BigDecimal destinationLongitude,

            @RequestParam(
                    defaultValue = "10"
            )
            BigDecimal searchRadiusKm
    ) {

        return ResponseEntity.ok(
                matchingService.findMatches(
                        pickupLatitude,
                        pickupLongitude,
                        destinationLatitude,
                        destinationLongitude,
                        searchRadiusKm
                )
        );
    }
}