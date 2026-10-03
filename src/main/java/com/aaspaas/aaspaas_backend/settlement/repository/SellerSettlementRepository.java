package com.aaspaas.aaspaas_backend.settlement.repository;

import com.aaspaas.aaspaas_backend.settlement.entity.SellerSettlement;
import com.aaspaas.aaspaas_backend.settlement.enums.SettlementStatus;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface SellerSettlementRepository
        extends JpaRepository<SellerSettlement, Long> {

    Optional<SellerSettlement>
    findBySettlementNumber(
            String settlementNumber
    );

    List<SellerSettlement>
    findByBusinessIdOrderByCreatedAtDesc(
            Long businessId
    );

    boolean existsByBusinessIdAndPeriodStartAndPeriodEnd(
            Long businessId,
            OffsetDateTime periodStart,
            OffsetDateTime periodEnd
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT s
        FROM SellerSettlement s
        WHERE s.id = :id
        """)
    Optional<SellerSettlement>
    findByIdForUpdate(
            @Param("id") Long id
    );

    long countByStatus(
            SettlementStatus status
    );
}