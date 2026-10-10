package com.aaspaas.aaspaas_backend.rating.entity;

import com.aaspaas.aaspaas_backend.business.entity.Business;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryAssignment;
import com.aaspaas.aaspaas_backend.order.entity.Order;
import com.aaspaas.aaspaas_backend.rating.enums.RatingStatus;
import com.aaspaas.aaspaas_backend.rating.enums.RatingTargetType;
import com.aaspaas.aaspaas_backend.user.entity.User;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "ratings",
        uniqueConstraints = {

                @UniqueConstraint(
                        name = "uk_rating_reviewer_order_business",
                        columnNames = {
                                "reviewer_id",
                                "order_id",
                                "target_business_id"
                        }
                ),

                @UniqueConstraint(
                        name = "uk_rating_reviewer_order_partner",
                        columnNames = {
                                "reviewer_id",
                                "order_id",
                                "target_user_id",
                                "delivery_assignment_id"
                        }
                )
        },
        indexes = {

                @Index(
                        name = "idx_ratings_order_id",
                        columnList = "order_id"
                ),

                @Index(
                        name = "idx_ratings_reviewer_id",
                        columnList = "reviewer_id"
                ),

                @Index(
                        name = "idx_ratings_business_id",
                        columnList = "target_business_id"
                ),

                @Index(
                        name = "idx_ratings_target_user_id",
                        columnList = "target_user_id"
                ),

                @Index(
                        name = "idx_ratings_assignment_id",
                        columnList = "delivery_assignment_id"
                ),

                @Index(
                        name = "idx_ratings_status",
                        columnList = "status"
                ),

                @Index(
                        name = "idx_ratings_created_at",
                        columnList = "created_at"
                )
        }
)
@Getter
@Setter
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =========================================================
    // WHO GAVE THE RATING
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "reviewer_id",
            nullable = false
    )
    private User reviewer;


    // =========================================================
    // ORDER
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "order_id",
            nullable = false
    )
    private Order order;


    // =========================================================
    // RATING TARGET
    // =========================================================

    @Enumerated(EnumType.STRING)
    @Column(
            name = "target_type",
            nullable = false,
            length = 30
    )
    private RatingTargetType targetType;


    // =========================================================
    // BUSINESS TARGET
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "target_business_id"
    )
    private Business targetBusiness;


    // =========================================================
    // USER TARGET
    //
    // For delivery partner:
    // target_user_id = deliveryPartner.user.id
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "target_user_id"
    )
    private User targetUser;


    // =========================================================
    // DELIVERY ASSIGNMENT
    //
    // Required when target = DELIVERY_PARTNER
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "delivery_assignment_id"
    )
    private DeliveryAssignment deliveryAssignment;


    // =========================================================
    // RATING
    // =========================================================

    @Column(
            name = "rating",
            nullable = false
    )
    private Short rating;


    // =========================================================
    // REVIEW
    // =========================================================

    @Column(
            name = "review",
            columnDefinition = "TEXT"
    )
    private String review;


    // =========================================================
    // MODERATION STATUS
    // =========================================================

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private RatingStatus status;


    // =========================================================
    // AUDIT
    // =========================================================

    @Column(
            name = "created_at",
            nullable = false
    )
    private OffsetDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private OffsetDateTime updatedAt;


    // =========================================================
    // PRE PERSIST
    // =========================================================

    @PrePersist
    protected void onCreate() {

        OffsetDateTime now =
                OffsetDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }

        if (status == null) {
            status = RatingStatus.ACTIVE;
        }
    }


    // =========================================================
    // PRE UPDATE
    // =========================================================

    @PreUpdate
    protected void onUpdate() {

        updatedAt =
                OffsetDateTime.now();
    }
}