package com.aaspaas.aaspaas_backend.settlement.dto;

import com.aaspaas.aaspaas_backend.settlement.enums.SettlementStatus;
import com.aaspaas.aaspaas_backend.settlement.enums.SettlementType;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Builder
public class SellerSettlementResponse {

    private Long id;

    private String settlementNumber;

    private Long businessId;

    private OffsetDateTime periodStart;

    private OffsetDateTime periodEnd;

    private BigDecimal grossSales;

    private BigDecimal refundAmount;

    private BigDecimal platformCommission;

    private BigDecimal adjustmentAmount;

    private BigDecimal netAmount;

    private SettlementStatus status;

    private SettlementType settlementType;

    private OffsetDateTime processedAt;

    private OffsetDateTime createdAt;
}