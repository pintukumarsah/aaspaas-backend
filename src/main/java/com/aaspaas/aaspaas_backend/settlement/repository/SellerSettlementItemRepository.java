package com.aaspaas.aaspaas_backend.settlement.repository;

import com.aaspaas.aaspaas_backend.settlement.entity.SellerSettlementItem;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SellerSettlementItemRepository
        extends JpaRepository<SellerSettlementItem, Long> {

    List<SellerSettlementItem>
    findBySettlementIdOrderByIdAsc(
            Long settlementId
    );

    boolean existsBySettlementIdAndOrderId(
            Long settlementId,
            Long orderId
    );
}