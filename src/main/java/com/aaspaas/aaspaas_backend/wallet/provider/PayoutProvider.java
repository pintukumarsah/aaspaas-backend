package com.aaspaas.aaspaas_backend.wallet.provider;

import java.math.BigDecimal;

public interface PayoutProvider {

    PayoutResult transfer(PayoutInstruction instruction);

    /** Verifies a provider webhook signature computed over the raw body. */
    boolean verifyWebhookSignature(String rawBody, String signature);

    String name();

    record PayoutInstruction(
            String reference,
            BigDecimal amount,
            String accountHolderName,
            String accountNumber,
            String ifscCode,
            String narration) {
    }

    enum Outcome { PROCESSING, PAID, FAILED }

    record PayoutResult(
            String providerPayoutId,
            Outcome outcome,
            String failureReason) {
    }
}
