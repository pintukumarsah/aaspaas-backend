package com.aaspaas.aaspaas_backend.admin.dto;

import com.aaspaas.aaspaas_backend.delivery.pricing.entity.DeliveryPricingRule;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Builder
public class DeliveryPricingRuleResponse {
    private Long id;
    private String ruleName;
    private String deliveryMode;
    private BigDecimal baseFee;
    private BigDecimal perKmFee;
    private BigDecimal minimumDeliveryFee;
    private BigDecimal maximumDeliveryFee;
    private BigDecimal commissionPercentage;
    private BigDecimal fixedPlatformFee;
    private String currency;
    private OffsetDateTime effectiveFrom;
    private OffsetDateTime effectiveTo;
    private boolean active;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static DeliveryPricingRuleResponse fromEntity(DeliveryPricingRule rule) {
        return DeliveryPricingRuleResponse.builder()
                .id(rule.getId())
                .ruleName(rule.getRuleName())
                .deliveryMode(rule.getDeliveryMode())
                .baseFee(rule.getBaseFee())
                .perKmFee(rule.getPerKmFee())
                .minimumDeliveryFee(rule.getMinimumDeliveryFee())
                .maximumDeliveryFee(rule.getMaximumDeliveryFee())
                .commissionPercentage(rule.getCommissionPercentage())
                .fixedPlatformFee(rule.getFixedPlatformFee())
                .currency(rule.getCurrency())
                .effectiveFrom(rule.getEffectiveFrom())
                .effectiveTo(rule.getEffectiveTo())
                .active(rule.isActive())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }
}
