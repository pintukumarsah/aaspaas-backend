package com.aaspaas.aaspaas_backend.commission.repository;

import com.aaspaas.aaspaas_backend.commission.entity.CommissionEntry;
import com.aaspaas.aaspaas_backend.commission.enums.CommissionStatus;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommissionEntryRepository
        extends JpaRepository<CommissionEntry, Long> {

    List<CommissionEntry>
    findByOrderIdOrderByCreatedAtAsc(
            Long orderId
    );

    List<CommissionEntry>
    findByBusinessIdAndStatusOrderByCreatedAtAsc(
            Long businessId,
            CommissionStatus status
    );

    List<CommissionEntry>
    findByPartnerIdAndStatusOrderByCreatedAtAsc(
            Long partnerId,
            CommissionStatus status
    );

    boolean existsByOrderIdAndCommissionType(
            Long orderId,
            String commissionType
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT c
        FROM CommissionEntry c
        WHERE c.id = :id
        """)
    Optional<CommissionEntry>
    findByIdForUpdate(
            @Param("id") Long id
    );

    @Query("""
        SELECT c
        FROM CommissionEntry c
        JOIN FETCH c.order o
        LEFT JOIN FETCH c.business b
        LEFT JOIN FETCH c.partner p
        WHERE c.business.id = :businessId
        AND c.status = :status
        AND c.createdAt >= :startDate
        AND c.createdAt < :endDate
        ORDER BY c.createdAt ASC
        """)
    List<CommissionEntry>
    findBusinessEntriesForPeriod(
            @Param("businessId") Long businessId,
            @Param("status") CommissionStatus status,
            @Param("startDate")
            java.time.OffsetDateTime startDate,
            @Param("endDate")
            java.time.OffsetDateTime endDate
    );
}