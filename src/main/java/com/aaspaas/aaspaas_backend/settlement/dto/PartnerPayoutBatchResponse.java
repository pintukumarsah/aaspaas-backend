package com.aaspaas.aaspaas_backend.settlement.dto;

import com.aaspaas.aaspaas_backend.settlement.enums.SettlementStatus;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Builder
public class PartnerPayoutBatchResponse {

    private Long id;

    private String batchNumber;

    private BigDecimal totalGrossAmount;

    private BigDecimal totalPlatformFee;

    private BigDecimal totalNetAmount;

    private Integer payoutCount;

    private SettlementStatus status;

    private OffsetDateTime processedAt;

    private OffsetDateTime createdAt;
}