package com.aaspaas.aaspaas_backend.settlement.entity;

import com.aaspaas.aaspaas_backend.settlement.enums.SettlementStatus;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "partner_payout_batches",
        indexes = {
                @Index(
                        name = "idx_partner_batch_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class PartnerPayoutBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "batch_number",
            nullable = false,
            unique = true,
            length = 50
    )
    private String batchNumber;

    @Column(
            name = "total_gross_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal totalGrossAmount =
            BigDecimal.ZERO;

    @Column(
            name = "total_platform_fee",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal totalPlatformFee =
            BigDecimal.ZERO;

    @Column(
            name = "total_net_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal totalNetAmount =
            BigDecimal.ZERO;

    @Column(
            name = "payout_count",
            nullable = false
    )
    private Integer payoutCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private SettlementStatus status;

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
    }
}