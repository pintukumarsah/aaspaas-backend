package com.aaspaas.aaspaas_backend.settlement.entity;

import com.aaspaas.aaspaas_backend.business.entity.Business;
import com.aaspaas.aaspaas_backend.settlement.enums.SettlementStatus;
import com.aaspaas.aaspaas_backend.settlement.enums.SettlementType;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "seller_settlements",
        indexes = {
                @Index(
                        name = "idx_seller_settlement_business",
                        columnList = "business_id"
                ),
                @Index(
                        name = "idx_seller_settlement_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_seller_settlement_period",
                        columnList = "period_start,period_end"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class SellerSettlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "settlement_number",
            nullable = false,
            unique = true,
            length = 50
    )
    private String settlementNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "business_id",
            nullable = false
    )
    private Business business;

    @Column(
            name = "period_start",
            nullable = false
    )
    private OffsetDateTime periodStart;

    @Column(
            name = "period_end",
            nullable = false
    )
    private OffsetDateTime periodEnd;

    @Column(
            name = "gross_sales",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal grossSales = BigDecimal.ZERO;

    @Column(
            name = "refund_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal refundAmount = BigDecimal.ZERO;

    @Column(
            name = "platform_commission",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal platformCommission = BigDecimal.ZERO;

    @Column(
            name = "adjustment_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal adjustmentAmount = BigDecimal.ZERO;

    @Column(
            name = "net_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal netAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private SettlementStatus status;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "settlement_type",
            nullable = false,
            length = 30
    )
    private SettlementType settlementType;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

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
            status = SettlementStatus.CREATED;
        }

        if (settlementType == null) {
            settlementType = SettlementType.REGULAR;
        }
    }
}