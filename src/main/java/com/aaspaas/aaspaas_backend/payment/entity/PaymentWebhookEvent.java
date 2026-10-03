package com.aaspaas.aaspaas_backend.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "payment_webhook_events",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_payment_webhook_provider_event",
                        columnNames = {
                                "provider",
                                "event_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_payment_webhook_event_type",
                        columnList = "event_type"
                ),
                @Index(
                        name = "idx_payment_webhook_status",
                        columnList = "processing_status"
                ),
                @Index(
                        name = "idx_payment_webhook_received_at",
                        columnList = "received_at"
                )
        }
)
@Getter
@Setter
public class PaymentWebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "provider",
            nullable = false,
            length = 30
    )
    private String provider;

    @Column(
            name = "event_id",
            nullable = false,
            length = 150
    )
    private String eventId;

    @Column(
            name = "event_type",
            nullable = false,
            length = 100
    )
    private String eventType;

    @Column(
            name = "payload_hash",
            nullable = false,
            length = 128
    )
    private String payloadHash;

    @Column(
            name = "processing_status",
            nullable = false,
            length = 30
    )
    private String processingStatus;

    @Column(
            name = "error_message",
            length = 1000
    )
    private String errorMessage;

    @Column(
            name = "received_at",
            nullable = false
    )
    private OffsetDateTime receivedAt;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    @PrePersist
    protected void onCreate() {

        if (receivedAt == null) {
            receivedAt = OffsetDateTime.now();
        }

        if (processingStatus == null) {
            processingStatus = "RECEIVED";
        }
    }
}