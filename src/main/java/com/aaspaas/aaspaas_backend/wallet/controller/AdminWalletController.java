package com.aaspaas.aaspaas_backend.wallet.controller;

import com.aaspaas.aaspaas_backend.common.dto.ApiResponse;
import com.aaspaas.aaspaas_backend.wallet.dto.*;
import com.aaspaas.aaspaas_backend.wallet.enums.BankVerificationStatus;
import com.aaspaas.aaspaas_backend.wallet.enums.WithdrawalStatus;
import com.aaspaas.aaspaas_backend.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/** Covered by the existing /api/admin/** ADMIN rule. */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminWalletController {

    private final WalletService walletService;

    @GetMapping("/bank-accounts")
    public ResponseEntity<ApiResponse<Page<BankAccountResponse>>> bankAccounts(
            @RequestParam(required = false) BankVerificationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                "Bank accounts fetched", walletService.listBankAccounts(status, page, size)));
    }

    @PatchMapping("/bank-accounts/{id}/verify")
    public ResponseEntity<ApiResponse<BankAccountResponse>> verify(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Bank account verified", walletService.verifyBankAccount(id)));
    }

    @PatchMapping("/bank-accounts/{id}/reject")
    public ResponseEntity<ApiResponse<BankAccountResponse>> reject(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Bank account rejected", walletService.rejectBankAccount(id)));
    }

    @GetMapping("/withdrawals")
    public ResponseEntity<ApiResponse<Page<WithdrawalResponse>>> withdrawals(
            @RequestParam(required = false) WithdrawalStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                "Withdrawals fetched", walletService.listWithdrawals(status, page, size)));
    }

    @PostMapping("/withdrawals/{id}/approve")
    public ResponseEntity<ApiResponse<WithdrawalResponse>> approve(
            @PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "Withdrawal approved", walletService.approveWithdrawal(id, principal.getName())));
    }

    @PostMapping("/withdrawals/{id}/reject")
    public ResponseEntity<ApiResponse<WithdrawalResponse>> rejectWithdrawal(
            @PathVariable Long id, @Valid @RequestBody DecisionRequest body, Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "Withdrawal rejected",
                walletService.rejectWithdrawal(id, body.getReason(), principal.getName())));
    }

    @PostMapping("/withdrawals/{id}/retry")
    public ResponseEntity<ApiResponse<WithdrawalResponse>> retry(
            @PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "Withdrawal retried", walletService.retryWithdrawal(id, principal.getName())));
    }

    @PostMapping("/withdrawals/{id}/cancel")
    public ResponseEntity<ApiResponse<WithdrawalResponse>> cancel(
            @PathVariable Long id, @Valid @RequestBody DecisionRequest body, Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "Withdrawal cancelled",
                walletService.cancelFailedWithdrawal(id, body.getReason(), principal.getName())));
    }
}
