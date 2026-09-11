package com.aaspaas.aaspaas_backend.order.entity;

import com.aaspaas.aaspaas_backend.product.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
    name = "order_items",
    indexes = {
        @Index(
            name = "idx_order_items_order_id",
            columnList = "order_id"
        ),
        @Index(
            name = "idx_order_items_product_id",
            columnList = "product_id"
        )
    }
)
@Getter
@Setter
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "order_id",
        nullable = false
    )
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "product_id"
    )
    private Product product;

    /*
     * Snapshot of product name at order time.
     */
    @Column(
        name = "product_name",
        nullable = false,
        length = 200
    )
    private String productName;

    @Column(
        nullable = false,
        precision = 12,
        scale = 3
    )
    private BigDecimal quantity;

    /*
     * Snapshot of product price at order time.
     */
    @Column(
        name = "unit_price",
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal unitPrice;

    @Column(
        name = "total_price",
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal totalPrice;
}