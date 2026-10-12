package com.aaspaas.aaspaas_backend.wallet;

import com.aaspaas.aaspaas_backend.common.exception.BusinessException;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryPartner;
import com.aaspaas.aaspaas_backend.delivery.repository.DeliveryPartnerRepository;
import com.aaspaas.aaspaas_backend.notification.service.NotificationService;
import com.aaspaas.aaspaas_backend.user.repository.UserRepository;
import com.aaspaas.aaspaas_backend.wallet.crypto.BankDataCipher;
import com.aaspaas.aaspaas_backend.wallet.dto.CreateWithdrawalRequest;
import com.aaspaas.aaspaas_backend.wallet.dto.WithdrawalResponse;
import com.aaspaas.aaspaas_backend.wallet.entity.PartnerBankAccount;
import com.aaspaas.aaspaas_backend.wallet.entity.PartnerWithdrawal;
import com.aaspaas.aaspaas_backend.wallet.enums.BankVerificationStatus;
import com.aaspaas.aaspaas_backend.wallet.enums.WithdrawalStatus;
import com.aaspaas.aaspaas_backend.wallet.provider.PayoutProvider;
import com.aaspaas.aaspaas_backend.wallet.repository.*;
import com.aaspaas.aaspaas_backend.wallet.serviceimpl.WalletServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WalletServiceImplTest {

    @Mock PartnerBankAccountRepository bankAccountRepository;
    @Mock PartnerWithdrawalRepository withdrawalRepository;
    @Mock PartnerWithdrawalItemRepository itemRepository;
    @Mock WalletPayoutRepository payoutRepository;
    @Mock DeliveryPartnerRepository partnerRepository;
    @Mock UserRepository userRepository;
    @Mock NotificationService notificationService;
    @Mock BankDataCipher cipher;
    @Mock PayoutProvider payoutProvider;
    @Mock ObjectMapper objectMapper;

    @InjectMocks WalletServiceImpl service;

    private DeliveryPartner partner;
    private PartnerBankAccount bank;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "minWithdrawalAmount", new BigDecimal("100"));
        ReflectionTestUtils.setField(service, "maxRetries", 3);

        partner = mock(DeliveryPartner.class);
        when(partner.getId()).thenReturn(5L);
        when(partner.getVerificationStatus()).thenReturn("APPROVED");
        when(partnerRepository.findByUserPhone("8888888888")).thenReturn(Optional.of(partner));
        when(partnerRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(partner));

        bank = new PartnerBankAccount();
        bank.setId(7L);
        bank.setPartner(partner);
        bank.setActive(true);
        bank.setAccountNumberLast4("9012");
        bank.setVerificationStatus(BankVerificationStatus.VERIFIED);
        when(bankAccountRepository.findByIdAndPartnerId(7L, 5L)).thenReturn(Optional.of(bank));
        when(withdrawalRepository.findByPartnerIdAndIdempotencyKey(anyLong(), any()))
                .thenReturn(Optional.empty());
    }

    private CreateWithdrawalRequest req() {
        CreateWithdrawalRequest r = new CreateWithdrawalRequest();
        r.setBankAccountId(7L);
        return r;
    }

    @Test
    void missingIdempotencyKeyIsRejected() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.requestWithdrawal(req(), null, "8888888888"));
        assertEquals(400, ex.getStatusCode());
        verifyNoInteractions(payoutRepository);
    }

    @Test
    void unverifiedBankAccountIsRejected() {
        bank.setVerificationStatus(BankVerificationStatus.PENDING);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.requestWithdrawal(req(), "key-12345678", "8888888888"));
        assertEquals(409, ex.getStatusCode());
    }

    @Test
    void noEligiblePayoutsMeansBelowMinimum() {
        when(payoutRepository.lockByPartnerAndStatus(anyLong(), any())).thenReturn(List.of());
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.requestWithdrawal(req(), "key-12345678", "8888888888"));
        assertEquals(409, ex.getStatusCode());
        verify(withdrawalRepository, never()).saveAndFlush(any());
    }

    @Test
    void sameIdempotencyKeyReturnsExistingWithdrawalWithoutTouchingPayouts() {
        PartnerWithdrawal existing = new PartnerWithdrawal();
        existing.setId(99L);
        existing.setPartner(partner);
        existing.setBankAccount(bank);
        existing.setAmount(new BigDecimal("250.00"));
        existing.setStatus(WithdrawalStatus.REQUESTED);
        when(withdrawalRepository.findByPartnerIdAndIdempotencyKey(5L, "key-12345678"))
                .thenReturn(Optional.of(existing));

        WithdrawalResponse res = service.requestWithdrawal(req(), "key-12345678", "8888888888");

        assertEquals(99L, res.getId());
        verify(payoutRepository, never()).lockByPartnerAndStatus(anyLong(), any());
    }

    @Test
    void unapprovedPartnerCannotWithdraw() {
        when(partner.getVerificationStatus()).thenReturn("PENDING");
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.requestWithdrawal(req(), "key-12345678", "8888888888"));
        assertEquals(403, ex.getStatusCode());
    }
}
