package com.aaspaas.aaspaas_backend.wallet.repository;

import com.aaspaas.aaspaas_backend.delivery.entity.PartnerPayout;
import com.aaspaas.aaspaas_backend.delivery.enums.PartnerPayoutStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * Wallet-specific view over partner_payouts. Kept separate so the existing
 * PartnerPayoutRepository does not need to be edited.
 */
public interface WalletPayoutRepository
        extends JpaRepository<PartnerPayout, Long> {

    /** Locks every row it returns, so concurrent withdrawals / batches cannot take the same payout. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT p FROM PartnerPayout p
        WHERE p.partner.id = :partnerId AND p.status = :status
        ORDER BY p.createdAt ASC
        """)
    List<PartnerPayout> lockByPartnerAndStatus(
            @Param("partnerId") Long partnerId,
            @Param("status") PartnerPayoutStatus status);

    @Query("""
        SELECT COALESCE(SUM(p.netAmount), 0) FROM PartnerPayout p
        WHERE p.partner.id = :partnerId AND p.status = :status
        """)
    BigDecimal sumNetByPartnerAndStatus(
            @Param("partnerId") Long partnerId,
            @Param("status") PartnerPayoutStatus status);
}
