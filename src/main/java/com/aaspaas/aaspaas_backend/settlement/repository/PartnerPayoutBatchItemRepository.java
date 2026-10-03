package com.aaspaas.aaspaas_backend.settlement.repository;

import com.aaspaas.aaspaas_backend.settlement.entity.PartnerPayoutBatchItem;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartnerPayoutBatchItemRepository
        extends JpaRepository<PartnerPayoutBatchItem, Long> {

    List<PartnerPayoutBatchItem>
    findByBatchIdOrderByIdAsc(
            Long batchId
    );

    boolean existsByPartnerPayoutId(
            Long partnerPayoutId
    );
}