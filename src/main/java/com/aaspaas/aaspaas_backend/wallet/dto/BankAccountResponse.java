package com.aaspaas.aaspaas_backend.wallet.dto;

import com.aaspaas.aaspaas_backend.wallet.enums.BankVerificationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class BankAccountResponse {
    private Long id;
    private Long partnerId;
    private String accountHolderName;
    private String maskedAccountNumber;
    private String ifscCode;
    private String bankName;
    private BankVerificationStatus verificationStatus;
    private boolean primary;
    private boolean active;
    private OffsetDateTime createdAt;
}
