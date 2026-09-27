package com.aaspaas.aaspaas_backend.delivery.repository;

import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryCompletionFinalization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryFinalizationRepository
        extends JpaRepository<DeliveryCompletionFinalization, Long> {

    Optional<DeliveryCompletionFinalization>
    findByDeliveryAssignmentId(Long deliveryAssignmentId);

    boolean existsByDeliveryAssignmentId(Long deliveryAssignmentId);
}