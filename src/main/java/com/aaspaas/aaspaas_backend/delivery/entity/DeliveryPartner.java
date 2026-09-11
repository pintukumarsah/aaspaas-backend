package com.aaspaas.aaspaas_backend.delivery.entity;

import com.aaspaas.aaspaas_backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
    name = "delivery_partners",
    indexes = {
        @Index(name = "idx_delivery_partner_user_id", columnList = "user_id"),
        @Index(name = "idx_delivery_partner_availability", columnList = "availability_status"),
        @Index(name = "idx_delivery_partner_verification", columnList = "verification_status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPartner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "vehicle_type", length = 30)
    private String vehicleType;

    @Column(name = "vehicle_number", length = 30)
    private String vehicleNumber;

    @Column(name = "verification_status", nullable = false, length = 30)
    private String verificationStatus;

    @Column(name = "availability_status", nullable = false, length = 30)
    private String availabilityStatus;

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal rating;

    @Column(name = "total_deliveries", nullable = false)
    private Integer totalDeliveries;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();

        if (verificationStatus == null) {
            verificationStatus = "PENDING";
        }

        if (availabilityStatus == null) {
            availabilityStatus = "OFFLINE";
        }

        if (rating == null) {
            rating = BigDecimal.ZERO;
        }

        if (totalDeliveries == null) {
            totalDeliveries = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}