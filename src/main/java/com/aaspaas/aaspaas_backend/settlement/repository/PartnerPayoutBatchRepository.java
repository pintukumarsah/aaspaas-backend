package com.aaspaas.aaspaas_backend.settlement.repository;

import com.aaspaas.aaspaas_backend.settlement.entity.PartnerPayoutBatch;
import com.aaspaas.aaspaas_backend.settlement.enums.SettlementStatus;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PartnerPayoutBatchRepository
        extends JpaRepository<PartnerPayoutBatch, Long> {

    Optional<PartnerPayoutBatch>
    findByBatchNumber(
            String batchNumber
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT b
        FROM PartnerPayoutBatch b
        WHERE b.id = :id
        """)
    Optional<PartnerPayoutBatch>
    findByIdForUpdate(
            @Param("id") Long id
    );

    long countByStatus(
            SettlementStatus status
    );
}