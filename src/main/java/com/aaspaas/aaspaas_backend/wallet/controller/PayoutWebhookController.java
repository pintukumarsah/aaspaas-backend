package com.aaspaas.aaspaas_backend.wallet.controller;

import com.aaspaas.aaspaas_backend.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public (no JWT) endpoint called by RazorpayX. Authenticity is enforced by the
 * HMAC signature check inside WalletService.handleProviderWebhook.
 */
@RestController
@RequestMapping("/api/payouts/webhook")
@RequiredArgsConstructor
public class PayoutWebhookController {

    private final WalletService walletService;

    @PostMapping("/razorpayx")
    public ResponseEntity<Void> receive(
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature,
            @RequestBody String rawBody) {
        walletService.handleProviderWebhook(rawBody, signature);
        return ResponseEntity.ok().build();
    }
}
