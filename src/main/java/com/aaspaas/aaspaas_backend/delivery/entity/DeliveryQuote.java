package com.aaspaas.aaspaas_backend.delivery.entity;

import com.aaspaas.aaspaas_backend.delivery.enums.DeliveryQuoteStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "delivery_quotes",
        indexes = {
                @Index(
                        name = "idx_delivery_quotes_request_id",
                        columnList = "delivery_request_id"
                ),
                @Index(
                        name = "idx_delivery_quotes_partner_id",
                        columnList = "partner_id"
                ),
                @Index(
                        name = "idx_delivery_quotes_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
public class DeliveryQuote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "delivery_request_id",
            nullable = false
    )
    private DeliveryRequest deliveryRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "partner_id",
            nullable = false
    )
    private DeliveryPartner partner;

    @Column(
            name = "quoted_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal quotedAmount;

    @Column(name = "estimated_minutes")
    private Integer estimatedMinutes;

    @Column(length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private DeliveryQuoteStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        OffsetDateTime now = OffsetDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }

        if (status == null) {
            status = DeliveryQuoteStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}