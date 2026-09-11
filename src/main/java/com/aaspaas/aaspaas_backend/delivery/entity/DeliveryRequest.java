package com.aaspaas.aaspaas_backend.delivery.entity;

import com.aaspaas.aaspaas_backend.order.entity.Order;
import com.aaspaas.aaspaas_backend.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
    name = "delivery_requests",
    indexes = {
        @Index(
            name = "idx_delivery_requests_order_id",
            columnList = "order_id"
        ),
        @Index(
            name = "idx_delivery_requests_customer_id",
            columnList = "customer_id"
        ),
        @Index(
            name = "idx_delivery_requests_status",
            columnList = "status"
        ),
        @Index(
            name = "idx_delivery_requests_requested_at",
            columnList = "requested_at"
        )
    }
)
@Getter
@Setter
public class DeliveryRequest {

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
        name = "customer_id",
        nullable = false
    )
    private User customer;

    @Column(
        name = "pickup_address_id",
        nullable = false
    )
    private Long pickupAddressId;

    @Column(
        name = "delivery_address_id",
        nullable = false
    )
    private Long deliveryAddressId;

    @Column(
        name = "max_budget",
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal maxBudget;

    @Column(
        name = "requested_at",
        nullable = false
    )
    private OffsetDateTime requestedAt;

    @Column(
        name = "expires_at",
        nullable = false
    )
    private OffsetDateTime expiresAt;

    @Column(
        name = "status",
        nullable = false,
        length = 30
    )
    private String status;

    @Column(
        name = "delivery_mode",
        nullable = false,
        length = 30
    )
    private String deliveryMode;

    @Column(name = "pickup_latitude", precision = 10, scale = 7)
    private BigDecimal pickupLatitude;

    @Column(name = "pickup_longitude", precision = 10, scale = 7)
    private BigDecimal pickupLongitude;

    @Column(name = "delivery_latitude", precision = 10, scale = 7)
    private BigDecimal deliveryLatitude;

    @Column(name = "delivery_longitude", precision = 10, scale = 7)
    private BigDecimal deliveryLongitude;

    @Column(name = "requested_departure_at")
    private OffsetDateTime requestedDepartureAt;

    @Column(name = "required_by_at")
    private OffsetDateTime requiredByAt;

    @Column(name = "search_radius_km", precision = 8, scale = 2)
    private BigDecimal searchRadiusKm;

    @Column(name = "customer_note", length = 500)
    private String customerNote;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        OffsetDateTime now = OffsetDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;

        if (this.status == null) {
            this.status = "OPEN";
        }

        if (this.deliveryMode == null) {
            this.deliveryMode = "DIRECT";
        }

        if (this.searchRadiusKm == null) {
            this.searchRadiusKm =
                    BigDecimal.valueOf(10);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}