package com.aaspaas.aaspaas_backend.delivery.pricing.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DeliveryPriceResponse {

    private BigDecimal roadDistanceKm;

    private BigDecimal suggestedDeliveryFee;

    private BigDecimal minimumDeliveryFee;

    private BigDecimal maximumDeliveryFee;

    private BigDecimal commissionPercentage;

    private BigDecimal estimatedPlatformCommission;

    private BigDecimal estimatedPartnerEarning;

    private String currency;

    private String pricingRule;
}