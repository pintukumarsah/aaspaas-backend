package com.aaspaas.aaspaas_backend.delivery.repository;

import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryTracking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryTrackingRepository
        extends JpaRepository<DeliveryTracking, Long> {

    List<DeliveryTracking>
    findByDeliveryAssignmentIdOrderByRecordedAtAsc(
            Long deliveryAssignmentId
    );

    Optional<DeliveryTracking>
    findTopByDeliveryAssignmentIdOrderByRecordedAtDesc(
            Long deliveryAssignmentId
    );
}