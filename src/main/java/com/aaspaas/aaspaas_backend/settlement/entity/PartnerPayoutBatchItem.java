package com.aaspaas.aaspaas_backend.settlement.entity;

import com.aaspaas.aaspaas_backend.delivery.entity.PartnerPayout;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "partner_payout_batch_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_partner_batch_payout",
                        columnNames = "partner_payout_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class PartnerPayoutBatchItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "batch_id",
            nullable = false
    )
    private PartnerPayoutBatch batch;

    @OneToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "partner_payout_id",
            nullable = false,
            unique = true
    )
    private PartnerPayout partnerPayout;

    @Column(
            name = "amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal amount;

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