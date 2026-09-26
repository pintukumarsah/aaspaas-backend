package com.aaspaas.aaspaas_backend.delivery.pricing.service;

import com.aaspaas.aaspaas_backend.delivery.pricing.dto.DeliveryPriceResponse;

import java.math.BigDecimal;

public interface DeliveryPricingService {

    DeliveryPriceResponse calculateSuggestedPrice(
            String deliveryMode,
            BigDecimal roadDistanceKm
    );

    BigDecimal calculatePlatformCommission(
            BigDecimal deliveryFee,
            String deliveryMode
    );
}