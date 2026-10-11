package com.aaspaas.aaspaas_backend.wallet.repository;

import com.aaspaas.aaspaas_backend.wallet.entity.PartnerWithdrawalItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartnerWithdrawalItemRepository
        extends JpaRepository<PartnerWithdrawalItem, Long> {

    List<PartnerWithdrawalItem> findByWithdrawalId(Long withdrawalId);
}
