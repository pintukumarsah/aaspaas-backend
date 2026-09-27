package com.aaspaas.aaspaas_backend.delivery.entity;

import com.aaspaas.aaspaas_backend.delivery.enums.PartnerPayoutStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "partner_payouts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "ux_partner_payout_assignment",
                        columnNames = "delivery_assignment_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_partner_payouts_partner_id",
                        columnList = "partner_id"
                ),
                @Index(
                        name = "idx_partner_payouts_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
public class PartnerPayout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "partner_id",
            nullable = false
    )
    private DeliveryPartner partner;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "delivery_assignment_id",
            nullable = false,
            unique = true
    )
    private DeliveryAssignment deliveryAssignment;

    @Column(
            name = "gross_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal grossAmount;

    @Column(
            name = "platform_fee",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal platformFee;

    @Column(
            name = "net_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal netAmount;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private PartnerPayoutStatus status;

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }

        if (status == null) {
            status = PartnerPayoutStatus.PENDING;
        }
    }
}