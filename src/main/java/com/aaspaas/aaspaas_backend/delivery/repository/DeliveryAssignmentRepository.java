package com.aaspaas.aaspaas_backend.delivery.repository;

import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryAssignment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
   import com.aaspaas.aaspaas_backend.delivery.enums.DeliveryAssignmentStatus;
import java.util.Optional;

public interface DeliveryAssignmentRepository
        extends JpaRepository<DeliveryAssignment, Long> {

    Optional<DeliveryAssignment>
    findByDeliveryRequestId(Long deliveryRequestId);

     boolean existsByDeliveryRequestId(Long deliveryRequestId); 

    Optional<DeliveryAssignment>
    findByQuoteId(Long quoteId);

    Optional<DeliveryAssignment>                                  // ADD THIS
    findFirstByPartnerIdAndStatusNotOrderByAssignedAtDesc(
            Long partnerId,
            DeliveryAssignmentStatus status
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
          AND da.status IN (
              com.aaspaas.aaspaas_backend.delivery.enums.DeliveryAssignmentStatus.ASSIGNED,
              com.aaspaas.aaspaas_backend.delivery.enums.DeliveryAssignmentStatus.ACCEPTED,
              com.aaspaas.aaspaas_backend.delivery.enums.DeliveryAssignmentStatus.PICKED_UP,
              com.aaspaas.aaspaas_backend.delivery.enums.DeliveryAssignmentStatus.OUT_FOR_DELIVERY,
              com.aaspaas.aaspaas_backend.delivery.enums.DeliveryAssignmentStatus.DELIVERY_OTP_SENT
          )
        """)
    long countActiveAssignmentsForPartner(
            @Param("partnerId") Long partnerId
    );
}