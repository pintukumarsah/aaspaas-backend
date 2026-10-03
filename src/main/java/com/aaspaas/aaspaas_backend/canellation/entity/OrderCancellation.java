package com.aaspaas.aaspaas_backend.cancellation.entity;

import com.aaspaas.aaspaas_backend.cancellation.enums.CancellationStatus;
import com.aaspaas.aaspaas_backend.order.entity.Order;
import com.aaspaas.aaspaas_backend.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "order_cancellations",
        indexes = {
                @Index(
                        name = "idx_order_cancellations_order_id",
                        columnList = "order_id"
                ),
                @Index(
                        name = "idx_order_cancellations_cancelled_by",
                        columnList = "cancelled_by"
                )
        }
)
@Getter
@Setter
public class OrderCancellation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "order_id",
            nullable = false,
            unique = true
    )
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "cancelled_by",
            nullable = false
    )
    private User cancelledBy;

    @Column(
            name = "cancelled_by_type",
            nullable = false,
            length = 30
    )
    private String cancelledByType;

    @Column(
            name = "reason",
            nullable = false,
            length = 500
    )
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private CancellationStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }

        if (status == null) {
            status = CancellationStatus.COMPLETED;
        }
    }
}