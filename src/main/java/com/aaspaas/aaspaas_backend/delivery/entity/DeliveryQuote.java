package com.aaspaas.aaspaas_backend.delivery.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
    name = "delivery_quotes",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_delivery_quote_request_partner",
            columnNames = {
                "delivery_request_id",
                "partner_id"
            }
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryQuote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "delivery_request_id",
        nullable = false
    )
    private DeliveryRequest deliveryRequest;

    @ManyToOne(fetch = FetchType.LAZY)
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

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();

        if (status == null) {
            status = "PENDING";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}