package com.aaspaas.aaspaas_backend.commission.entity;

import com.aaspaas.aaspaas_backend.commission.enums.CommissionStatus;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryAssignment;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryPartner;
import com.aaspaas.aaspaas_backend.business.entity.Business;
import com.aaspaas.aaspaas_backend.order.entity.Order;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "commission_entries",
        indexes = {
                @Index(
                        name = "idx_commission_order",
                        columnList = "order_id"
                ),
                @Index(
                        name = "idx_commission_assignment",
                        columnList = "delivery_assignment_id"
                ),
                @Index(
                        name = "idx_commission_business",
                        columnList = "business_id"
                ),
                @Index(
                        name = "idx_commission_partner",
                        columnList = "partner_id"
                ),
                @Index(
                        name = "idx_commission_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class CommissionEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "order_id",
            nullable = false
    )
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "delivery_assignment_id"
    )
    private DeliveryAssignment deliveryAssignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "business_id"
    )
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "partner_id"
    )
    private DeliveryPartner partner;

    @Column(
            name = "commission_type",
            nullable = false,
            length = 40
    )
    private String commissionType;

    @Column(
            name = "gross_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal grossAmount;

    @Column(
            name = "commission_rate",
            precision = 5,
            scale = 2
    )
    private BigDecimal commissionRate;

    @Column(
            name = "commission_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal commissionAmount;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private CommissionStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "reversal_of_id"
    )
    private CommissionEntry reversalOf;

    @Column(
            name = "description",
            length = 500
    )
    private String description;

    @Column(
            name = "created_at",
            nullable = false
    )
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }

        if (status == null) {
            status = CommissionStatus.PENDING;
        }

        if (grossAmount == null) {
            grossAmount = BigDecimal.ZERO;
        }

        if (commissionAmount == null) {
            commissionAmount = BigDecimal.ZERO;
        }
    }
}