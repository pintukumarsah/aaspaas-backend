package com.aaspaas.aaspaas_backend.delivery.pricing.controller;

import com.aaspaas.aaspaas_backend.delivery.pricing.dto.DeliveryPriceResponse;
import com.aaspaas.aaspaas_backend.delivery.pricing.service.DeliveryPricingService;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/delivery/pricing")
@RequiredArgsConstructor
@Validated
public class DeliveryPricingController {

    private final DeliveryPricingService pricingService;

    @GetMapping("/calculate")
    public DeliveryPriceResponse calculatePrice(

            @RequestParam
            @NotBlank
            String deliveryMode,

            @RequestParam
            @DecimalMin("0.0")
            @DecimalMax("1000.0")
            BigDecimal roadDistanceKm
    ) {

        return pricingService.calculateSuggestedPrice(
                deliveryMode,
                roadDistanceKm
        );
    }
}