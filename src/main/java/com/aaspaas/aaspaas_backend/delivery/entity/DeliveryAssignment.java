package com.aaspaas.aaspaas_backend.delivery.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import com.aaspaas.aaspaas_backend.delivery.pricing.entity.DeliveryPricingRule;

@Entity
@Table(
    name = "delivery_assignments",
    indexes = {
        @Index(name = "idx_delivery_assignment_request_id", columnList = "delivery_request_id"),
        @Index(name = "idx_delivery_assignment_partner_id", columnList = "partner_id"),
        @Index(name = "idx_delivery_assignment_status", columnList = "status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delivery_request_id", nullable = false)
    private DeliveryRequest deliveryRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partner_id", nullable = false)
    private DeliveryPartner partner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id")
    private DeliveryQuote quote;

    @Column(name = "assigned_at", nullable = false)
    private OffsetDateTime assignedAt;

    @Column(name = "accepted_at")
    private OffsetDateTime acceptedAt;

    @Column(name = "picked_up_at")
    private OffsetDateTime pickedUpAt;

    @Column(name = "delivered_at")
    private OffsetDateTime deliveredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private DeliveryAssignmentStatus status;

    @Column(
            name = "agreed_delivery_fee",
            precision = 12,
            scale = 2
    )
    private BigDecimal agreedDeliveryFee;

    @Column(
            name = "platform_commission_amount",
            precision = 12,
            scale = 2
    )
    private BigDecimal platformCommissionAmount;

    @Column(
            name = "partner_earning_amount",
            precision = 12,
            scale = 2
    )
    private BigDecimal partnerEarningAmount;

    @Column(
            name = "currency",
            nullable = false,
            length = 3
    )
    @Builder.Default
    private String currency = "INR";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pricing_rule_id")
    private DeliveryPricingRule pricingRule;

    @PrePersist
    protected void onCreate() {

        if (assignedAt == null) {
            assignedAt = OffsetDateTime.now();
        }

        if (status == null) {
            status = DeliveryAssignmentStatus.ASSIGNED;
        }
    }
}