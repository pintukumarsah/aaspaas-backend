package com.aaspaas.aaspaas_backend.wallet.controller;

import com.aaspaas.aaspaas_backend.common.dto.ApiResponse;
import com.aaspaas.aaspaas_backend.wallet.dto.*;
import com.aaspaas.aaspaas_backend.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/** Partner-facing wallet APIs. Restrict to ROLE_DELIVERY_PARTNER in SecurityConfig. */
@RestController
@RequestMapping("/api/partner-wallet")
@RequiredArgsConstructor
public class PartnerWalletController {

    private final WalletService walletService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<WalletSummaryResponse>> summary(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "Wallet summary fetched", walletService.getSummary(principal.getName())));
    }

    @PostMapping("/bank-accounts")
    public ResponseEntity<ApiResponse<BankAccountResponse>> addBankAccount(
            @Valid @RequestBody AddBankAccountRequest request, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Bank account added. It will be usable after admin verification.",
                walletService.addBankAccount(request, principal.getName())));
    }

    @GetMapping("/bank-accounts")
    public ResponseEntity<ApiResponse<List<BankAccountResponse>>> myBankAccounts(
            Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "Bank accounts fetched", walletService.getMyBankAccounts(principal.getName())));
    }

    @DeleteMapping("/bank-accounts/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable Long id, Principal principal) {
        walletService.deactivateBankAccount(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Bank account removed", null));
    }

    @PostMapping("/withdrawals")
    public ResponseEntity<ApiResponse<WithdrawalResponse>> requestWithdrawal(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CreateWithdrawalRequest request,
            Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Withdrawal requested",
                walletService.requestWithdrawal(request, idempotencyKey, principal.getName())));
    }

    @GetMapping("/withdrawals")
    public ResponseEntity<ApiResponse<Page<WithdrawalResponse>>> myWithdrawals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "Withdrawals fetched",
                walletService.getMyWithdrawals(principal.getName(), page, size)));
    }
}
