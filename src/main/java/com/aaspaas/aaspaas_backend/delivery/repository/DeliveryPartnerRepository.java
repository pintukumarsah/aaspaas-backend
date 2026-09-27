package com.aaspaas.aaspaas_backend.delivery.repository;

import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryPartner;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryPartnerAvailabilityStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface DeliveryPartnerRepository
        extends JpaRepository<DeliveryPartner, Long> {

    Optional<DeliveryPartner> findByUserId(Long userId);

    Optional<DeliveryPartner> findByUserPhone(String phone);

    boolean existsByUserId(Long userId);

    /**
     * Finds route-enabled partners who are currently ONLINE
     * and have complete location + destination information.
     */
    @Query("""
        SELECT dp
        FROM DeliveryPartner dp
        WHERE dp.availabilityStatus = :availabilityStatus
          AND dp.verificationStatus = 'APPROVED'
          AND dp.routeAvailable = true
          AND dp.currentLatitude IS NOT NULL
          AND dp.currentLongitude IS NOT NULL
          AND dp.destinationLatitude IS NOT NULL
          AND dp.destinationLongitude IS NOT NULL
          AND dp.locationUpdatedAt >= :minimumLocationTime
    """)
    List<DeliveryPartner> findEligibleRoutePartners(
            @Param("minimumLocationTime")
            OffsetDateTime minimumLocationTime,
            @Param("availabilityStatus")
            DeliveryPartnerAvailabilityStatus availabilityStatus
    );
    @Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("""
    SELECT dp
    FROM DeliveryPartner dp
    JOIN FETCH dp.user u
    WHERE dp.id = :id
    """)
Optional<DeliveryPartner> findByIdForUpdate(
        @Param("id") Long id
);
}