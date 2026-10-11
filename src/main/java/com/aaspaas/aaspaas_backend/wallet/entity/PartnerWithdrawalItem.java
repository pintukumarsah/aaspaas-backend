package com.aaspaas.aaspaas_backend.wallet.entity;

import com.aaspaas.aaspaas_backend.delivery.entity.PartnerPayout;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** Links a withdrawal to the delivery payouts it settles. A payout can be in only one withdrawal. */
@Entity
@Table(
        name = "partner_withdrawal_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_withdrawal_item_payout", columnNames = "partner_payout_id")
)
@Getter
@Setter
public class PartnerWithdrawalItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "withdrawal_id", nullable = false)
    private PartnerWithdrawal withdrawal;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partner_payout_id", nullable = false)
    private PartnerPayout partnerPayout;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;
}
