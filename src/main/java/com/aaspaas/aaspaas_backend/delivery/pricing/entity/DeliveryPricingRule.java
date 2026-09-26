package com.aaspaas.aaspaas_backend.delivery.pricing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "delivery_pricing_rules",
        indexes = {
                @Index(
                        name = "idx_delivery_pricing_rules_active",
                        columnList = "is_active"
                ),
                @Index(
                        name = "idx_delivery_pricing_rules_mode",
                        columnList = "delivery_mode"
                ),
                @Index(
                        name = "idx_delivery_pricing_rules_effective",
                        columnList = "effective_from,effective_to"
                )
        }
)
@Getter
@Setter
public class DeliveryPricingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;

    @Column(name = "delivery_mode", nullable = false, length = 30)
    private String deliveryMode;

    @Column(
            name = "base_fee",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal baseFee;

    @Column(
            name = "per_km_fee",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal perKmFee;

    @Column(
            name = "minimum_delivery_fee",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal minimumDeliveryFee;

    @Column(
            name = "maximum_delivery_fee",
            precision = 12,
            scale = 2
    )
    private BigDecimal maximumDeliveryFee;

    @Column(
            name = "commission_percentage",
            nullable = false,
            precision = 5,
            scale = 2
    )
    private BigDecimal commissionPercentage;

    @Column(
            name = "fixed_platform_fee",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal fixedPlatformFee;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "effective_from", nullable = false)
    private OffsetDateTime effectiveFrom;

    @Column(name = "effective_to")
    private OffsetDateTime effectiveTo;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        OffsetDateTime now = OffsetDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}