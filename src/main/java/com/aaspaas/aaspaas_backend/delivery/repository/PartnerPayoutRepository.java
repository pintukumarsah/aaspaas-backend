package com.aaspaas.aaspaas_backend.delivery.repository;

import com.aaspaas.aaspaas_backend.delivery.entity.PartnerPayout;
import com.aaspaas.aaspaas_backend.delivery.enums.PartnerPayoutStatus;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PartnerPayoutRepository
        extends JpaRepository<PartnerPayout, Long> {

    boolean existsByDeliveryAssignmentId(
            Long deliveryAssignmentId
    );

    Optional<PartnerPayout>
    findByDeliveryAssignmentId(
            Long deliveryAssignmentId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT p
        FROM PartnerPayout p
        JOIN FETCH p.partner partner
        JOIN FETCH p.deliveryAssignment assignment
        WHERE p.id = :id
        """)
    Optional<PartnerPayout>
    findByIdForUpdate(
            @Param("id") Long id
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT p
        FROM PartnerPayout p
        JOIN FETCH p.partner partner
        JOIN FETCH p.deliveryAssignment assignment
        WHERE p.deliveryAssignment.id = :assignmentId
        """)
    Optional<PartnerPayout>
    findByDeliveryAssignmentIdForUpdate(
            @Param("assignmentId")
            Long assignmentId
    );

    List<PartnerPayout>
    findByStatusOrderByCreatedAtAsc(
            PartnerPayoutStatus status
    );

    List<PartnerPayout>
    findByPartnerIdAndStatusOrderByCreatedAtAsc(
            Long partnerId,
            PartnerPayoutStatus status
    );
}