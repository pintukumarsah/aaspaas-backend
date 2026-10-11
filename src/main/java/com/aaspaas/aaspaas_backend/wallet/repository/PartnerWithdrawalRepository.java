package com.aaspaas.aaspaas_backend.wallet.repository;

import com.aaspaas.aaspaas_backend.wallet.entity.PartnerWithdrawal;
import com.aaspaas.aaspaas_backend.wallet.enums.WithdrawalStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PartnerWithdrawalRepository
        extends JpaRepository<PartnerWithdrawal, Long> {

    Optional<PartnerWithdrawal> findByPartnerIdAndIdempotencyKey(Long partnerId, String key);

    Page<PartnerWithdrawal> findByPartnerIdOrderByCreatedAtDesc(Long partnerId, Pageable pageable);

    Page<PartnerWithdrawal> findByStatus(WithdrawalStatus status, Pageable pageable);

    Optional<PartnerWithdrawal> findByProviderPayoutId(String providerPayoutId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM PartnerWithdrawal w WHERE w.id = :id")
    Optional<PartnerWithdrawal> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM PartnerWithdrawal w WHERE w.providerPayoutId = :pid")
    Optional<PartnerWithdrawal> findByProviderPayoutIdForUpdate(@Param("pid") String pid);
}
