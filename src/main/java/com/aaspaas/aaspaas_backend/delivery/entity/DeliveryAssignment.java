package com.aaspaas.aaspaas_backend.delivery.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(
    name = "delivery_assignments",
    indexes = {
        @Index(
            name = "idx_assignment_partner",
            columnList = "partner_id"
        ),
        @Index(
            name = "idx_assignment_status",
            columnList = "status"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "delivery_request_id",
        nullable = false,
        unique = true
    )
    private DeliveryRequest deliveryRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "partner_id",
        nullable = false
    )
    private DeliveryPartner partner;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "quote_id",
        nullable = false,
        unique = true
    )
    private DeliveryQuote quote;

    @Column(
        name = "assigned_at",
        nullable = false
    )
    private OffsetDateTime assignedAt;

    @Column(name = "accepted_at")
    private OffsetDateTime acceptedAt;

    @Column(name = "picked_up_at")
    private OffsetDateTime pickedUpAt;

    @Column(name = "delivered_at")
    private OffsetDateTime deliveredAt;

    @Column(nullable = false, length = 30)
    private String status;

    @PrePersist
    protected void onCreate() {

        if (assignedAt == null) {
            assignedAt = OffsetDateTime.now();
        }

        if (status == null) {
            status = "ASSIGNED";
        }
    }
}