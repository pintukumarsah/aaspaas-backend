package com.aaspaas.aaspaas_backend.delivery.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
    name = "delivery_tracking",
    indexes = {
        @Index(
            name = "idx_delivery_tracking_assignment",
            columnList = "delivery_assignment_id"
        ),
        @Index(
            name = "idx_delivery_tracking_recorded_at",
            columnList = "recorded_at"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryTracking {

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
        nullable = false,
        precision = 10,
        scale = 7
    )
    private BigDecimal latitude;

    @Column(
        nullable = false,
        precision = 10,
        scale = 7
    )
    private BigDecimal longitude;

    @Column(
        name = "accuracy_meters",
        precision = 8,
        scale = 2
    )
    private BigDecimal accuracyMeters;

    @Column(
        name = "recorded_at",
        nullable = false
    )
    private OffsetDateTime recordedAt;

    @PrePersist
    protected void onCreate() {

        if (recordedAt == null) {
            recordedAt = OffsetDateTime.now();
        }
    }
}