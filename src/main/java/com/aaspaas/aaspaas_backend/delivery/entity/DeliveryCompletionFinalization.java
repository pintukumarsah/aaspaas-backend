package com.aaspaas.aaspaas_backend.delivery.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(
    name = "delivery_completion_finalizations",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_completion_assignment",
            columnNames = "delivery_assignment_id"
        )
    },
    indexes = {
        @Index(
            name = "idx_completion_order",
            columnList = "order_id"
        ),
        @Index(
            name = "idx_completion_partner",
            columnList = "partner_id"
        ),
        @Index(
            name = "idx_completion_finalized_at",
            columnList = "finalized_at"
        )
    }
)
@Getter
@Setter
public class DeliveryCompletionFinalization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        name = "delivery_assignment_id",
        nullable = false
    )
    private Long deliveryAssignmentId;

    @Column(
        name = "order_id",
        nullable = false
    )
    private Long orderId;

    @Column(
        name = "partner_id",
        nullable = false
    )
    private Long partnerId;

    @Column(
        name = "finalized_at",
        nullable = false
    )
    private OffsetDateTime finalizedAt;

    @Column(name = "finalized_by")
    private Long finalizedBy;

    @Column(
        nullable = false,
        length = 30
    )
    private String status;

    @Column(length = 500)
    private String notes;

    @Column(
        name = "created_at",
        nullable = false
    )
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {

        if (finalizedAt == null) {
            finalizedAt = OffsetDateTime.now();
        }

        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }

        if (status == null) {
            status = "COMPLETED";
        }
    }
}