package com.aaspaas.aaspaas_backend.wallet.serviceimpl;

import com.aaspaas.aaspaas_backend.common.exception.BusinessException;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryPartner;
import com.aaspaas.aaspaas_backend.delivery.entity.PartnerPayout;
import com.aaspaas.aaspaas_backend.delivery.enums.PartnerPayoutStatus;
import com.aaspaas.aaspaas_backend.delivery.repository.DeliveryPartnerRepository;
import com.aaspaas.aaspaas_backend.notification.service.NotificationService;
import com.aaspaas.aaspaas_backend.user.entity.User;
import com.aaspaas.aaspaas_backend.user.repository.UserRepository;
import com.aaspaas.aaspaas_backend.wallet.crypto.BankDataCipher;
import com.aaspaas.aaspaas_backend.wallet.dto.*;
import com.aaspaas.aaspaas_backend.wallet.entity.PartnerBankAccount;
import com.aaspaas.aaspaas_backend.wallet.entity.PartnerWithdrawal;
import com.aaspaas.aaspaas_backend.wallet.entity.PartnerWithdrawalItem;
import com.aaspaas.aaspaas_backend.wallet.enums.BankVerificationStatus;
import com.aaspaas.aaspaas_backend.wallet.enums.WithdrawalStatus;
import com.aaspaas.aaspaas_backend.wallet.provider.PayoutProvider;
import com.aaspaas.aaspaas_backend.wallet.provider.PayoutProviderException;
import com.aaspaas.aaspaas_backend.wallet.repository.*;
import com.aaspaas.aaspaas_backend.wallet.service.WalletService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private static final int MAX_BANK_ACCOUNTS = 5;

    private final PartnerBankAccountRepository bankAccountRepository;
    private final PartnerWithdrawalRepository withdrawalRepository;
    private final PartnerWithdrawalItemRepository itemRepository;
    private final WalletPayoutRepository payoutRepository;
    private final DeliveryPartnerRepository partnerRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final BankDataCipher cipher;
    private final PayoutProvider payoutProvider;
    private final ObjectMapper objectMapper;

    @Value("${wallet.min-withdrawal-amount:100}")
    private BigDecimal minWithdrawalAmount;

    @Value("${wallet.max-retries:3}")
    private int maxRetries;

    // =====================================================================
    // PARTNER: BANK ACCOUNTS
    // =====================================================================

    @Override
    @Transactional
    public BankAccountResponse addBankAccount(AddBankAccountRequest request, String phone) {
        DeliveryPartner partner = approvedPartner(phone);

        if (bankAccountRepository.countByPartnerIdAndActiveTrue(partner.getId()) >= MAX_BANK_ACCOUNTS) {
            throw new BusinessException("Maximum " + MAX_BANK_ACCOUNTS + " bank accounts allowed", 409);
        }

        String number = request.getAccountNumber().trim();
        String fingerprint = cipher.fingerprint(number);
        if (bankAccountRepository.existsByPartnerIdAndAccountNumberHash(partner.getId(), fingerprint)) {
            throw new BusinessException("This bank account is already added", 409);
        }

        PartnerBankAccount acc = new PartnerBankAccount();
        acc.setPartner(partner);
        acc.setAccountHolderName(request.getAccountHolderName().trim());
        acc.setAccountNumberEncrypted(cipher.encrypt(number));
        acc.setAccountNumberLast4(number.substring(number.length() - 4));
        acc.setAccountNumberHash(fingerprint);
        acc.setIfscCode(request.getIfscCode().trim().toUpperCase(Locale.ROOT));
        acc.setBankName(request.getBankName() == null ? null : request.getBankName().trim());
        acc.setVerificationStatus(BankVerificationStatus.PENDING);
        acc.setActive(true);
        acc.setPrimary(bankAccountRepository.countByPartnerIdAndActiveTrue(partner.getId()) == 0);

        try {
            return toResponse(bankAccountRepository.saveAndFlush(acc));
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException("This bank account is already added", 409);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankAccountResponse> getMyBankAccounts(String phone) {
        DeliveryPartner partner = partnerByPhone(phone);
        return bankAccountRepository
                .findByPartnerIdAndActiveTrueOrderByCreatedAtDesc(partner.getId())
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void deactivateBankAccount(Long bankAccountId, String phone) {
        DeliveryPartner partner = partnerByPhone(phone);
        PartnerBankAccount acc = bankAccountRepository
                .findByIdAndPartnerId(bankAccountId, partner.getId())
                .orElseThrow(() -> new BusinessException("Bank account not found", 404));
        acc.setActive(false);
        acc.setPrimary(false);
        bankAccountRepository.save(acc);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletSummaryResponse getSummary(String phone) {
        DeliveryPartner partner = partnerByPhone(phone);
        return WalletSummaryResponse.builder()
                .availableBalance(payoutRepository.sumNetByPartnerAndStatus(
                        partner.getId(), PartnerPayoutStatus.ELIGIBLE))
                .inProcessAmount(payoutRepository.sumNetByPartnerAndStatus(
                        partner.getId(), PartnerPayoutStatus.PROCESSING))
                .totalPaid(payoutRepository.sumNetByPartnerAndStatus(
                        partner.getId(), PartnerPayoutStatus.PAID))
                .minimumWithdrawalAmount(minWithdrawalAmount)
                .build();
    }

    // =====================================================================
    // PARTNER: WITHDRAWAL REQUEST
    // =====================================================================

    @Override
    @Transactional
    public WithdrawalResponse requestWithdrawal(
            CreateWithdrawalRequest request, String idempotencyKey, String phone) {

        if (idempotencyKey == null || idempotencyKey.isBlank()
                || idempotencyKey.length() < 8 || idempotencyKey.length() > 100) {
            throw new BusinessException(
                    "Idempotency-Key header (8-100 characters) is required", 400);
        }

        DeliveryPartner partner = approvedPartner(phone);

        // Serialise concurrent requests of the same partner.
        partnerRepository.findByIdForUpdate(partner.getId())
                .orElseThrow(() -> new BusinessException("Delivery partner not found", 404));

        PartnerWithdrawal existing = withdrawalRepository
                .findByPartnerIdAndIdempotencyKey(partner.getId(), idempotencyKey).orElse(null);
        if (existing != null) {
            return toResponse(existing);
        }

        PartnerBankAccount bank = bankAccountRepository
                .findByIdAndPartnerId(request.getBankAccountId(), partner.getId())
                .orElseThrow(() -> new BusinessException("Bank account not found", 404));
        if (!bank.isActive()) {
            throw new BusinessException("Bank account is not active", 409);
        }
        if (bank.getVerificationStatus() != BankVerificationStatus.VERIFIED) {
            throw new BusinessException("Bank account is not verified yet", 409);
        }

        List<PartnerPayout> payouts = payoutRepository
                .lockByPartnerAndStatus(partner.getId(), PartnerPayoutStatus.ELIGIBLE);
        BigDecimal total = payouts.stream()
                .map(PartnerPayout::getNetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (payouts.isEmpty() || total.compareTo(minWithdrawalAmount) < 0) {
            throw new BusinessException(
                    "Minimum withdrawal amount is " + minWithdrawalAmount.toPlainString()
                            + ". Available: " + total.toPlainString(), 409);
        }

        PartnerWithdrawal w = new PartnerWithdrawal();
        w.setPartner(partner);
        w.setBankAccount(bank);
        w.setAmount(total);
        w.setStatus(WithdrawalStatus.REQUESTED);
        w.setIdempotencyKey(idempotencyKey);
        try {
            w = withdrawalRepository.saveAndFlush(w);
        } catch (DataIntegrityViolationException ex) {
            return toResponse(withdrawalRepository
                    .findByPartnerIdAndIdempotencyKey(partner.getId(), idempotencyKey)
                    .orElseThrow(() -> new BusinessException("Duplicate withdrawal request", 409)));
        }

        for (PartnerPayout p : payouts) {
            PartnerWithdrawalItem item = new PartnerWithdrawalItem();
            item.setWithdrawal(w);
            item.setPartnerPayout(p);
            item.setAmount(p.getNetAmount());
            itemRepository.save(item);

            p.setStatus(PartnerPayoutStatus.PROCESSING);
            payoutRepository.save(p);
        }

        return toResponse(w);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WithdrawalResponse> getMyWithdrawals(String phone, int page, int size) {
        DeliveryPartner partner = partnerByPhone(phone);
        return withdrawalRepository
                .findByPartnerIdOrderByCreatedAtDesc(partner.getId(), pageable(page, size))
                .map(this::toResponse);
    }

    // =====================================================================
    // ADMIN: BANK ACCOUNTS
    // =====================================================================

    @Override
    @Transactional(readOnly = true)
    public Page<BankAccountResponse> listBankAccounts(
            BankVerificationStatus status, int page, int size) {
        BankVerificationStatus s = status == null ? BankVerificationStatus.PENDING : status;
        return bankAccountRepository.findByVerificationStatus(s, pageable(page, size))
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public BankAccountResponse verifyBankAccount(Long id) {
        PartnerBankAccount acc = lockedBank(id);
        if (!acc.isActive()) {
            throw new BusinessException("Bank account is not active", 409);
        }
        acc.setVerificationStatus(BankVerificationStatus.VERIFIED);
        return toResponse(bankAccountRepository.save(acc));
    }

    @Override
    @Transactional
    public BankAccountResponse rejectBankAccount(Long id) {
        PartnerBankAccount acc = lockedBank(id);
        acc.setVerificationStatus(BankVerificationStatus.REJECTED);
        return toResponse(bankAccountRepository.save(acc));
    }

    // =====================================================================
    // ADMIN: WITHDRAWALS
    // =====================================================================

    @Override
    @Transactional(readOnly = true)
    public Page<WithdrawalResponse> listWithdrawals(WithdrawalStatus status, int page, int size) {
        WithdrawalStatus s = status == null ? WithdrawalStatus.REQUESTED : status;
        return withdrawalRepository.findByStatus(s, pageable(page, size)).map(this::toResponse);
    }

    @Override
    @Transactional
    public WithdrawalResponse approveWithdrawal(Long id, String adminPhone) {
        PartnerWithdrawal w = lockedWithdrawal(id);
        if (w.getStatus() != WithdrawalStatus.REQUESTED) {
            throw new BusinessException("Only a REQUESTED withdrawal can be approved", 409);
        }
        w.setStatus(WithdrawalStatus.APPROVED);
        w.setReviewedByUserId(userId(adminPhone));
        w.setReviewedAt(OffsetDateTime.now());
        w.setProviderReference("WD-" + w.getId());
        withdrawalRepository.save(w);

        return toResponse(transfer(w));
    }

    @Override
    @Transactional
    public WithdrawalResponse rejectWithdrawal(Long id, String reason, String adminPhone) {
        PartnerWithdrawal w = lockedWithdrawal(id);
        if (w.getStatus() != WithdrawalStatus.REQUESTED) {
            throw new BusinessException("Only a REQUESTED withdrawal can be rejected", 409);
        }
        releasePayouts(w);
        w.setStatus(WithdrawalStatus.REJECTED);
        w.setFailureReason(trim(reason));
        w.setReviewedByUserId(userId(adminPhone));
        w.setReviewedAt(OffsetDateTime.now());
        withdrawalRepository.save(w);
        notifyPartner(w, "Withdrawal rejected",
                "Your withdrawal of " + w.getAmount().toPlainString() + " was rejected.");
        return toResponse(w);
    }

    @Override
    @Transactional
    public WithdrawalResponse retryWithdrawal(Long id, String adminPhone) {
        PartnerWithdrawal w = lockedWithdrawal(id);
        if (w.getStatus() != WithdrawalStatus.FAILED) {
            throw new BusinessException("Only a FAILED withdrawal can be retried", 409);
        }
        if (w.getRetryCount() >= maxRetries) {
            throw new BusinessException("Maximum retry attempts reached", 409);
        }
        w.setRetryCount(w.getRetryCount() + 1);
        w.setReviewedByUserId(userId(adminPhone));
        w.setReviewedAt(OffsetDateTime.now());
        return toResponse(transfer(w));
    }

    @Override
    @Transactional
    public WithdrawalResponse cancelFailedWithdrawal(Long id, String reason, String adminPhone) {
        PartnerWithdrawal w = lockedWithdrawal(id);
        if (w.getStatus() != WithdrawalStatus.FAILED) {
            throw new BusinessException("Only a FAILED withdrawal can be cancelled", 409);
        }
        releasePayouts(w);
        w.setStatus(WithdrawalStatus.CANCELLED);
        w.setFailureReason(trim(reason));
        w.setReviewedByUserId(userId(adminPhone));
        w.setReviewedAt(OffsetDateTime.now());
        withdrawalRepository.save(w);
        notifyPartner(w, "Withdrawal cancelled",
                "Your withdrawal was cancelled and the amount is back in your balance.");
        return toResponse(w);
    }

    // =====================================================================
    // PROVIDER WEBHOOK
    // =====================================================================

    @Override
    @Transactional
    public void handleProviderWebhook(String rawBody, String signature) {
        if (!payoutProvider.verifyWebhookSignature(rawBody, signature)) {
            throw new BusinessException("Invalid webhook signature", 400);
        }
        try {
            JsonNode root = objectMapper.readTree(rawBody);
            String event = root.path("event").asText("");
            JsonNode entity = root.path("payload").path("payout").path("entity");
            String providerPayoutId = entity.path("id").asText(null);
            if (providerPayoutId == null || !event.startsWith("payout.")) {
                return; // not a payout event we care about
            }

            PartnerWithdrawal w = withdrawalRepository
                    .findByProviderPayoutIdForUpdate(providerPayoutId).orElse(null);
            if (w == null) {
                log.warn("Webhook for unknown payout {}", providerPayoutId);
                return;
            }

            switch (event) {
                case "payout.processed" -> markPaid(w);
                case "payout.failed", "payout.rejected", "payout.reversed" ->
                        markFailed(w, "Provider reported " + event.substring("payout.".length()));
                default -> { /* intermediate states ignored */ }
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("Unable to process payout webhook", 400);
        }
    }

    // =====================================================================
    // INTERNALS
    // =====================================================================

    /** Sends the money. Caller must hold the row lock on the withdrawal. */
    private PartnerWithdrawal transfer(PartnerWithdrawal w) {
        PartnerBankAccount bank = w.getBankAccount();
        if (w.getProviderReference() == null) {
            w.setProviderReference("WD-" + w.getId());
        }
        w.setStatus(WithdrawalStatus.PROCESSING);
        w.setFailureReason(null);
        withdrawalRepository.save(w);

        try {
            PayoutProvider.PayoutResult result = payoutProvider.transfer(
                    new PayoutProvider.PayoutInstruction(
                            w.getProviderReference(),
                            w.getAmount(),
                            bank.getAccountHolderName(),
                            cipher.decrypt(bank.getAccountNumberEncrypted()),
                            bank.getIfscCode(),
                            "AASPAAS payout " + w.getProviderReference()));

            w.setProviderPayoutId(result.providerPayoutId());
            withdrawalRepository.save(w);

            switch (result.outcome()) {
                case PAID -> markPaid(w);
                case FAILED -> markFailed(w, result.failureReason());
                case PROCESSING -> { /* wait for webhook */ }
            }
        } catch (PayoutProviderException ex) {
            // Outcome unknown: same provider reference is reused on retry,
            // so the provider's idempotency prevents a double payout.
            log.error("Payout provider error for withdrawal {}: {}", w.getId(), ex.getMessage());
            markFailed(w, "Provider error, will need retry");
        }
        return w;
    }

    private void markPaid(PartnerWithdrawal w) {
        if (w.getStatus() == WithdrawalStatus.PAID) {
            return;
        }
        if (w.getStatus() == WithdrawalStatus.CANCELLED || w.getStatus() == WithdrawalStatus.REJECTED) {
            log.error("Provider reported PAID for {} withdrawal {}. Reconcile manually.",
                    w.getStatus(), w.getId());
            return;
        }
        OffsetDateTime now = OffsetDateTime.now();
        for (PartnerWithdrawalItem item : itemRepository.findByWithdrawalId(w.getId())) {
            PartnerPayout p = item.getPartnerPayout();
            p.setStatus(PartnerPayoutStatus.PAID);
            p.setPaidAt(now);
            payoutRepository.save(p);
        }
        w.setStatus(WithdrawalStatus.PAID);
        w.setProcessedAt(now);
        w.setFailureReason(null);
        withdrawalRepository.save(w);
        notifyPartner(w, "Withdrawal paid",
                w.getAmount().toPlainString() + " has been sent to your bank account ending "
                        + w.getBankAccount().getAccountNumberLast4() + ".");
    }

    private void markFailed(PartnerWithdrawal w, String reason) {
        if (w.getStatus() == WithdrawalStatus.PAID
                || w.getStatus() == WithdrawalStatus.CANCELLED
                || w.getStatus() == WithdrawalStatus.REJECTED) {
            return;
        }
        // Payouts stay PROCESSING (locked) so the amount can be retried or cancelled by admin.
        w.setStatus(WithdrawalStatus.FAILED);
        w.setFailureReason(trim(reason));
        withdrawalRepository.save(w);
        notifyPartner(w, "Withdrawal delayed",
                "Your withdrawal could not be completed yet. Our team is looking into it.");
    }

    private void releasePayouts(PartnerWithdrawal w) {
        for (PartnerWithdrawalItem item : itemRepository.findByWithdrawalId(w.getId())) {
            PartnerPayout p = item.getPartnerPayout();
            if (p.getStatus() == PartnerPayoutStatus.PROCESSING) {
                p.setStatus(PartnerPayoutStatus.ELIGIBLE);
                payoutRepository.save(p);
            }
            itemRepository.delete(item);
        }
    }

    private void notifyPartner(PartnerWithdrawal w, String title, String message) {
        try {
            notificationService.createNotification(
                    w.getPartner().getUser().getId(), title, message,
                    "WITHDRAWAL", "WITHDRAWAL", w.getId());
        } catch (RuntimeException ex) {
            log.warn("Could not create notification for withdrawal {}", w.getId(), ex);
        }
    }

    private DeliveryPartner partnerByPhone(String phone) {
        return partnerRepository.findByUserPhone(phone)
                .orElseThrow(() -> new BusinessException("Delivery partner profile not found", 404));
    }

    private DeliveryPartner approvedPartner(String phone) {
        DeliveryPartner p = partnerByPhone(phone);
        if (!"APPROVED".equalsIgnoreCase(p.getVerificationStatus())) {
            throw new BusinessException("Delivery partner is not approved", 403);
        }
        return p;
    }

    private PartnerBankAccount lockedBank(Long id) {
        return bankAccountRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BusinessException("Bank account not found", 404));
    }

    private PartnerWithdrawal lockedWithdrawal(Long id) {
        return withdrawalRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BusinessException("Withdrawal not found", 404));
    }

    private Long userId(String phone) {
        User u = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException("User not found", 404));
        return u.getId();
    }

    private PageRequest pageable(int page, int size) {
        if (page < 0) {
            throw new BusinessException("Page cannot be negative", 400);
        }
        if (size < 1 || size > 100) {
            throw new BusinessException("Page size must be between 1 and 100", 400);
        }
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private String trim(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.length() > 500 ? t.substring(0, 500) : t;
    }

    private BankAccountResponse toResponse(PartnerBankAccount a) {
        return BankAccountResponse.builder()
                .id(a.getId())
                .partnerId(a.getPartner().getId())
                .accountHolderName(a.getAccountHolderName())
                .maskedAccountNumber("XXXXXX" + a.getAccountNumberLast4())
                .ifscCode(a.getIfscCode())
                .bankName(a.getBankName())
                .verificationStatus(a.getVerificationStatus())
                .primary(a.isPrimary())
                .active(a.isActive())
                .createdAt(a.getCreatedAt())
                .build();
    }

    private WithdrawalResponse toResponse(PartnerWithdrawal w) {
        return WithdrawalResponse.builder()
                .id(w.getId())
                .partnerId(w.getPartner().getId())
                .bankAccountId(w.getBankAccount().getId())
                .maskedAccountNumber("XXXXXX" + w.getBankAccount().getAccountNumberLast4())
                .amount(w.getAmount())
                .status(w.getStatus())
                .retryCount(w.getRetryCount())
                .failureReason(w.getFailureReason())
                .createdAt(w.getCreatedAt())
                .reviewedAt(w.getReviewedAt())
                .processedAt(w.getProcessedAt())
                .build();
    }
}
