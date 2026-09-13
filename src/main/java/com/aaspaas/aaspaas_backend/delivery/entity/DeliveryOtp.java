package com.aaspaas.aaspaas_backend.delivery.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(
    name = "delivery_otps",
    indexes = {
        @Index(
            name = "idx_delivery_otp_assignment",
            columnList = "delivery_assignment_id"
        ),
        @Index(
            name = "idx_delivery_otp_type",
            columnList = "otp_type"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "delivery_assignment_id",
        nullable = false
    )
    private DeliveryAssignment deliveryAssignment;

    @Column(
        name = "otp_type",
        nullable = false,
        length = 30
    )
    private String otpType;

    @Column(
        name = "otp_hash",
        nullable = false,
        length = 255
    )
    private String otpHash;

    @Column(
        name = "expires_at",
        nullable = false
    )
    private OffsetDateTime expiresAt;

    @Column(name = "verified_at")
    private OffsetDateTime verifiedAt;

    @Column(
        name = "attempt_count",
        nullable = false
    )
    private Integer attemptCount;

    @Column(
        name = "created_at",
        nullable = false
    )
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {

        createdAt = OffsetDateTime.now();

        if (attemptCount == null) {
            attemptCount = 0;
        }
    }
}