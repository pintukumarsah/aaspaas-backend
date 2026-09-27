package com.aaspaas.aaspaas_backend.delivery.repository;

import com.aaspaas.aaspaas_backend.delivery.entity.PartnerPayout;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartnerPayoutRepository
        extends JpaRepository<PartnerPayout, Long> {

    boolean existsByDeliveryAssignmentId(
            Long deliveryAssignmentId
    );
}