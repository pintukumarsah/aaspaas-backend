package com.aaspaas.aaspaas_backend.wallet.provider;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Development provider. Marks every transfer as PAID without moving money.
 * Active only when payout.provider=mock (default). NEVER use in production.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "payout.provider", havingValue = "mock", matchIfMissing = true)
public class MockPayoutProvider implements PayoutProvider {

    @PostConstruct
    void warn() {
        log.warn("MockPayoutProvider is active: no real money is transferred. "
                + "Set payout.provider=razorpayx for production.");
    }

    @Override
    public PayoutResult transfer(PayoutInstruction instruction) {
        return new PayoutResult("MOCK-" + instruction.reference(), Outcome.PAID, null);
    }

    @Override
    public boolean verifyWebhookSignature(String rawBody, String signature) {
        return false;
    }

    @Override
    public String name() {
        return "MOCK";
    }
}
