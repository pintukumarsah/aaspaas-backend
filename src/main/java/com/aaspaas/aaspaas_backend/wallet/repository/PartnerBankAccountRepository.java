package com.aaspaas.aaspaas_backend.wallet.repository;

import com.aaspaas.aaspaas_backend.wallet.entity.PartnerBankAccount;
import com.aaspaas.aaspaas_backend.wallet.enums.BankVerificationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PartnerBankAccountRepository
        extends JpaRepository<PartnerBankAccount, Long> {

    List<PartnerBankAccount> findByPartnerIdAndActiveTrueOrderByCreatedAtDesc(Long partnerId);

    Optional<PartnerBankAccount> findByIdAndPartnerId(Long id, Long partnerId);

    boolean existsByPartnerIdAndAccountNumberHash(Long partnerId, String hash);

    long countByPartnerIdAndActiveTrue(Long partnerId);

    Page<PartnerBankAccount> findByVerificationStatus(
            BankVerificationStatus status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM PartnerBankAccount b WHERE b.id = :id")
    Optional<PartnerBankAccount> findByIdForUpdate(@Param("id") Long id);
}
