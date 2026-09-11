package com.aaspaas.aaspaas_backend.order.entity;

import com.aaspaas.aaspaas_backend.business.entity.Business;
import com.aaspaas.aaspaas_backend.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "orders",
    indexes = {
        @Index(
            name = "idx_orders_customer_id",
            columnList = "customer_id"
        ),
        @Index(
            name = "idx_orders_business_id",
            columnList = "business_id"
        ),
        @Index(
            name = "idx_orders_order_status",
            columnList = "order_status"
        ),
        @Index(
            name = "idx_orders_created_at",
            columnList = "created_at"
        )
    }
)
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        name = "order_number",
        nullable = false,
        unique = true,
        length = 30
    )
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "customer_id",
        nullable = false
    )
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "business_id",
        nullable = false
    )
    private Business business;

    @Column(
        name = "delivery_address_id",
        nullable = false
    )
    private Long deliveryAddressId;

    @Column(
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal subtotal;

    @Column(
        name = "delivery_fee",
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal deliveryFee;

    @Column(
        name = "platform_fee",
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal platformFee;

    @Column(
        name = "discount_amount",
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal discountAmount;

    @Column(
        name = "total_amount",
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal totalAmount;

    @Column(
        name = "payment_status",
        nullable = false,
        length = 30
    )
    private String paymentStatus;

    @Column(
        name = "order_status",
        nullable = false,
        length = 30
    )
    private String orderStatus;

    @OneToMany(
        mappedBy = "order",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        OffsetDateTime now = OffsetDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;

        if (this.paymentStatus == null) {
            this.paymentStatus = "PENDING";
        }

        if (this.orderStatus == null) {
            this.orderStatus = "PENDING";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
