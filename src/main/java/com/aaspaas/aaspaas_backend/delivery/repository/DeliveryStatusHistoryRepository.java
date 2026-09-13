package com.aaspaas.aaspaas_backend.delivery.repository;

import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryStatusHistoryRepository
        extends JpaRepository<DeliveryStatusHistory, Long> {

    List<DeliveryStatusHistory>
    findByDeliveryAssignmentIdOrderByCreatedAtAsc(
            Long deliveryAssignmentId
    );
}