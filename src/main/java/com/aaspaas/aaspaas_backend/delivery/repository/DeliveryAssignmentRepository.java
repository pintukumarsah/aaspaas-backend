package com.aaspaas.aaspaas_backend.delivery.repository;

import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryAssignmentRepository
        extends JpaRepository<DeliveryAssignment, Long> {

    Optional<DeliveryAssignment>
    findByDeliveryRequestId(Long deliveryRequestId);

    Optional<DeliveryAssignment>
    findByQuoteId(Long quoteId);

    boolean existsByDeliveryRequestId(Long deliveryRequestId);
}