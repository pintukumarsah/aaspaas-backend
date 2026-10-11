package com.aaspaas.aaspaas_backend.wallet.entity;

import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryPartner;
import com.aaspaas.aaspaas_backend.wallet.enums.BankVerificationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "partner_bank_accounts",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_partner_bank_hash", columnNames = {"partner_id", "account_number_hash"}),
        indexes = {
                @Index(name = "idx_partner_bank_partner", columnList = "partner_id"),
                @Index(name = "idx_partner_bank_status", columnList = "verification_status")
        }
)
@Getter
@Setter
public class PartnerBankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partner_id", nullable = false)
    private DeliveryPartner partner;

    @Column(name = "account_holder_name", nullable = false, length = 150)
    private String accountHolderName;

    /** AES-GCM encrypted account number (Base64). Never returned by APIs. */
    @Column(name = "account_number_encrypted", nullable = false, columnDefinition = "TEXT")
    private String accountNumberEncrypted;

    @Column(name = "account_number_last4", nullable = false, length = 4)
    private String accountNumberLast4;

    /** Keyed HMAC fingerprint of the account number, used only to block duplicates per partner. */
    @Column(name = "account_number_hash", nullable = false, length = 64)
    private String accountNumberHash;

    @Column(name = "ifsc_code", nullable = false, length = 11)
    private String ifscCode;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    private BankVerificationStatus verificationStatus;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (verificationStatus == null) {
            verificationStatus = BankVerificationStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
