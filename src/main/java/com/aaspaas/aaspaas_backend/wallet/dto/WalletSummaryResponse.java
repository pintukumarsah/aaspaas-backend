package com.aaspaas.aaspaas_backend.wallet.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class WalletSummaryResponse {
    /** ELIGIBLE payouts: can be withdrawn now. */
    private BigDecimal availableBalance;
    /** PROCESSING payouts: locked in a withdrawal. */
    private BigDecimal inProcessAmount;
    /** PAID payouts: lifetime paid. */
    private BigDecimal totalPaid;
    private BigDecimal minimumWithdrawalAmount;
}
