package com.aaspaas.aaspaas_backend.order.dispute.entity;

import com.aaspaas.aaspaas_backend.order.entity.Order;
import com.aaspaas.aaspaas_backend.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "order_disputes", indexes = {
        @Index(name = "idx_order_disputes_status_created", columnList = "status, created_at"),
        @Index(name = "idx_order_disputes_order_id", columnList = "order_id"),
        @Index(name = "idx_order_disputes_raised_by", columnList = "raised_by_user_id")
})
@Getter
@Setter
public class OrderDispute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "raised_by_user_id", nullable = false)
    private User raisedBy;

    @Column(name = "reason_code", nullable = false, length = 40)
    private String reasonCode;

    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Column(name = "requested_refund_amount", precision = 12, scale = 2)
    private BigDecimal requestedRefundAmount;

    @Column(name = "approved_refund_amount", precision = 12, scale = 2)
    private BigDecimal approvedRefundAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "INR";

    @Column(name = "status", nullable = false, length = 30)
    private String status = "OPEN";

    @Column(name = "resolution_code", length = 40)
    private String resolutionCode;

    @Column(name = "resolution_notes", length = 2000)
    private String resolutionNotes;

    @Column(name = "reviewed_by_user_id")
    private Long reviewedByUserId;

    @Column(name = "reviewed_at")
    private OffsetDateTime reviewedAt;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (status == null) status = "OPEN";
        if (currency == null) currency = "INR";
    }

    @PreUpdate
    void onUpdate() { updatedAt = OffsetDateTime.now(); }
}
