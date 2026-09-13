package com.aaspaas.aaspaas_backend.delivery.entity;

import com.aaspaas.aaspaas_backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
    name = "delivery_status_history",
    indexes = {
        @Index(
            name = "idx_status_history_assignment",
            columnList = "delivery_assignment_id"
        ),
        @Index(
            name = "idx_status_history_created_at",
            columnList = "created_at"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryStatusHistory {

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
        length = 30
    )
    private String status;

    @Column(
        precision = 10,
        scale = 7
    )
    private BigDecimal latitude;

    @Column(
        precision = 10,
        scale = 7
    )
    private BigDecimal longitude;

    @Column(length = 500)
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(
        name = "created_at",
        nullable = false
    )
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}