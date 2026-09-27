package com.aaspaas.aaspaas_backend.delivery.repository;

import com.aaspaas.aaspaas_backend.delivery.entity.PartnerPayout;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PartnerPayoutRepository
        extends JpaRepository<PartnerPayout, Long> {

    boolean existsByDeliveryAssignmentId(
            Long deliveryAssignmentId
    );

    Optional<PartnerPayout> findByDeliveryAssignmentId(
            Long deliveryAssignmentId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT pp
        FROM PartnerPayout pp
        JOIN FETCH pp.partner p
        JOIN FETCH pp.deliveryAssignment da
        WHERE da.id = :assignmentId
    """)
    Optional<PartnerPayout> findByDeliveryAssignmentIdForUpdate(
            @Param("assignmentId") Long assignmentId
    );
}