package com.aaspaas.aaspaas_backend.wallet.service;

import com.aaspaas.aaspaas_backend.wallet.dto.*;
import com.aaspaas.aaspaas_backend.wallet.enums.BankVerificationStatus;
import com.aaspaas.aaspaas_backend.wallet.enums.WithdrawalStatus;
import org.springframework.data.domain.Page;

import java.util.List;

public interface WalletService {

    // ---- partner ----
    BankAccountResponse addBankAccount(AddBankAccountRequest request, String phone);

    List<BankAccountResponse> getMyBankAccounts(String phone);

    void deactivateBankAccount(Long bankAccountId, String phone);

    WalletSummaryResponse getSummary(String phone);

    WithdrawalResponse requestWithdrawal(
            CreateWithdrawalRequest request, String idempotencyKey, String phone);

    Page<WithdrawalResponse> getMyWithdrawals(String phone, int page, int size);

    // ---- admin ----
    Page<BankAccountResponse> listBankAccounts(BankVerificationStatus status, int page, int size);

    BankAccountResponse verifyBankAccount(Long bankAccountId);

    BankAccountResponse rejectBankAccount(Long bankAccountId);

    Page<WithdrawalResponse> listWithdrawals(WithdrawalStatus status, int page, int size);

    WithdrawalResponse approveWithdrawal(Long withdrawalId, String adminPhone);

    WithdrawalResponse rejectWithdrawal(Long withdrawalId, String reason, String adminPhone);

    WithdrawalResponse retryWithdrawal(Long withdrawalId, String adminPhone);

    WithdrawalResponse cancelFailedWithdrawal(Long withdrawalId, String reason, String adminPhone);

    // ---- provider webhook ----
    void handleProviderWebhook(String rawBody, String signature);
}
