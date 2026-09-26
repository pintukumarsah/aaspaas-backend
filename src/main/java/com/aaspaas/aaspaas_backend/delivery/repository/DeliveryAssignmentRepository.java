package com.aaspaas.aaspaas_backend.delivery.repository;

import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryAssignment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DeliveryAssignmentRepository
        extends JpaRepository<DeliveryAssignment, Long> {

    Optional<DeliveryAssignment> findByDeliveryRequestId(
            Long deliveryRequestId
    );

    Optional<DeliveryAssignment> findByQuoteId(
            Long quoteId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT da
        FROM DeliveryAssignment da
        JOIN FETCH da.deliveryRequest dr
        JOIN FETCH da.partner p
        WHERE da.id = :id
        """)
    Optional<DeliveryAssignment> findByIdForUpdate(
            @Param("id") Long id
    );

    @Query("""
        SELECT COUNT(da)
        FROM DeliveryAssignment da
        WHERE da.partner.id = :partnerId
          AND da.status IN ('ASSIGNED', 'ACCEPTED', 'PICKED_UP', 'OUT_FOR_DELIVERY')
        """)
    long countActiveAssignmentsForPartner(
            @Param("partnerId") Long partnerId
    );
}