package com.aaspaas.aaspaas_backend.wallet.dto;

import com.aaspaas.aaspaas_backend.wallet.enums.WithdrawalStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Builder
public class WithdrawalResponse {
    private Long id;
    private Long partnerId;
    private Long bankAccountId;
    private String maskedAccountNumber;
    private BigDecimal amount;
    private WithdrawalStatus status;
    private int retryCount;
    private String failureReason;
    private OffsetDateTime createdAt;
    private OffsetDateTime reviewedAt;
    private OffsetDateTime processedAt;
}
