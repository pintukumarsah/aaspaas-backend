package com.aaspaas.aaspaas_backend.delivery.entity;

import com.aaspaas.aaspaas_backend.delivery.enums.DeliveryAssignmentStatus;
import com.aaspaas.aaspaas_backend.delivery.pricing.entity.DeliveryPricingRule;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "delivery_assignments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "ux_delivery_assignment_request",
                        columnNames = "delivery_request_id"
                ),
                @UniqueConstraint(
                        name = "ux_delivery_assignment_quote",
                        columnNames = "quote_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_delivery_assignments_partner_id",
                        columnList = "partner_id"
                ),
                @Index(
                        name = "idx_delivery_assignments_status",
                        columnList = "status"
                )
        }
)
@Builder            // ADD THIS
@NoArgsConstructor  // ADD THIS
@AllArgsConstructor // ADD THIS
@Getter
@Setter
public class DeliveryAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "delivery_request_id",
            nullable = false,
            unique = true
    )
    private DeliveryRequest deliveryRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "partner_id",
            nullable = false
    )
    private DeliveryPartner partner;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "quote_id",
            nullable = false,
            unique = true
    )
    private DeliveryQuote quote;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private DeliveryAssignmentStatus status;

    @Column(name = "assigned_at", nullable = false)
    private OffsetDateTime assignedAt;

    @Column(name = "accepted_at")
    private OffsetDateTime acceptedAt;

    @Column(name = "picked_up_at")
    private OffsetDateTime pickedUpAt;

    @Column(name = "delivered_at")
    private OffsetDateTime deliveredAt;

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

        if (currency == null) {
            currency = "INR";
        }
    }
}