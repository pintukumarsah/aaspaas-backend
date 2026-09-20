package com.aaspaas.aaspaas_backend.delivery.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(
    name = "delivery_assignments",
    indexes = {
        @Index(name = "idx_delivery_assignment_request_id", columnList = "delivery_request_id"),
        @Index(name = "idx_delivery_assignment_partner_id", columnList = "partner_id"),
        @Index(name = "idx_delivery_assignment_status", columnList = "status")
    }
)
@Getter
@Setter
public class DeliveryAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delivery_request_id", nullable = false)
    private DeliveryRequest deliveryRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partner_id", nullable = false)
    private DeliveryPartner partner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id")
    private DeliveryQuote quote;

    @Column(name = "assigned_at", nullable = false)
    private OffsetDateTime assignedAt;

    @Column(name = "accepted_at")
    private OffsetDateTime acceptedAt;

    @Column(name = "picked_up_at")
    private OffsetDateTime pickedUpAt;

    @Column(name = "delivered_at")
    private OffsetDateTime deliveredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private DeliveryAssignmentStatus status;

    @PrePersist
    protected void onCreate() {

        if (assignedAt == null) {
            assignedAt = OffsetDateTime.now();
        }

        if (status == null) {
            status = DeliveryAssignmentStatus.ASSIGNED;
        }
    }
}