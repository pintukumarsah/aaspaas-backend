package com.aaspaas.aaspaas_backend.settlement.service;

import com.aaspaas.aaspaas_backend.settlement.dto.PartnerPayoutBatchResponse;
import com.aaspaas.aaspaas_backend.settlement.dto.SellerSettlementResponse;

import java.time.OffsetDateTime;
import java.util.List;

public interface SettlementService {

    SellerSettlementResponse createSellerSettlement(
            Long businessId,
            OffsetDateTime periodStart,
            OffsetDateTime periodEnd
    );

    SellerSettlementResponse getSellerSettlement(
            Long settlementId
    );

    List<SellerSettlementResponse>
    getBusinessSettlements(
            Long businessId
    );

    SellerSettlementResponse processSellerSettlement(
            Long settlementId
    );

    PartnerPayoutBatchResponse
    createPartnerPayoutBatch();

    PartnerPayoutBatchResponse
    getPartnerPayoutBatch(
            Long batchId
    );

    PartnerPayoutBatchResponse
    processPartnerPayoutBatch(
            Long batchId
    );
}