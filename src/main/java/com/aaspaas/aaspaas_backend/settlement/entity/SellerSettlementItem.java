package com.aaspaas.aaspaas_backend.settlement.entity;

import com.aaspaas.aaspaas_backend.order.entity.Order;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "seller_settlement_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_seller_settlement_order",
                        columnNames = {
                                "settlement_id",
                                "order_id"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class SellerSettlementItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "settlement_id",
            nullable = false
    )
    private SellerSettlement settlement;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "order_id",
            nullable = false
    )
    private Order order;

    @Column(
            name = "gross_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal grossAmount;

    @Column(
            name = "refund_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal refundAmount;

    @Column(
            name = "commission_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal commissionAmount;

    @Column(
            name = "adjustment_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal adjustmentAmount;

    @Column(
            name = "net_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal netAmount;

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
    }
}