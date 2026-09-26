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
                @Index(
                        name = "idx_delivery_partner_user_id",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_delivery_partner_availability",
                        columnList = "availability_status"
                ),
                @Index(
                        name = "idx_delivery_partner_verification",
                        columnList = "verification_status"
                ),
                @Index(
                        name = "idx_delivery_partner_route_available",
                        columnList = "route_available"
                ),
                @Index(
                        name = "idx_delivery_partner_location",
                        columnList = "current_latitude,current_longitude"
                )
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
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true
    )
    private User user;

    @Column(name = "vehicle_type", length = 30)
    private String vehicleType;

    @Column(name = "vehicle_number", length = 30)
    private String vehicleNumber;

    @Column(
            name = "verification_status",
            nullable = false,
            length = 30
    )
    private String verificationStatus;

//     @Column(
//             name = "availability_status",
//             nullable = false,
//             length = 30
//     )
//     private String availabilityStatus;

@Enumerated(EnumType.STRING)
@Column(
        name = "availability_status",
        nullable = false,
        length = 30
)
private DeliveryPartnerAvailabilityStatus availabilityStatus;

    @Column(
            nullable = false,
            precision = 3,
            scale = 2
    )
    private BigDecimal rating;

    @Column(
            name = "total_deliveries",
            nullable = false
    )
    private Integer totalDeliveries;

    // =========================
    // Current Location
    // =========================

    @Column(
            name = "current_latitude",
            precision = 10,
            scale = 7
    )
    private BigDecimal currentLatitude;

    @Column(
            name = "current_longitude",
            precision = 10,
            scale = 7
    )
    private BigDecimal currentLongitude;

    // =========================
    // Planned Destination
    // =========================

    @Column(
            name = "destination_latitude",
            precision = 10,
            scale = 7
    )
    private BigDecimal destinationLatitude;

    @Column(
            name = "destination_longitude",
            precision = 10,
            scale = 7
    )
    private BigDecimal destinationLongitude;

    @Column(
            name = "destination_name",
            length = 150
    )
    private String destinationName;

    @Column(name = "planned_departure_at")
    private OffsetDateTime plannedDepartureAt;

    @Column(
            name = "route_available",
            nullable = false
    )
    private Boolean routeAvailable;

    @Column(name = "location_updated_at")
    private OffsetDateTime locationUpdatedAt;

    // =========================
    // Audit
    // =========================

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

    @PrePersist
    protected void onCreate() {

        OffsetDateTime now = OffsetDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (verificationStatus == null) {
            verificationStatus = "PENDING";
        }

        // if (availabilityStatus == null) {
        //     availabilityStatus = "OFFLINE";
        // }

        if (availabilityStatus == null) {
    availabilityStatus = DeliveryPartnerAvailabilityStatus.OFFLINE;
}

        if (rating == null) {
            rating = BigDecimal.ZERO;
        }

        if (totalDeliveries == null) {
            totalDeliveries = 0;
        }

        if (routeAvailable == null) {
            routeAvailable = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}