package com.aaspaas.aaspaas_backend.marketplace.repository;

import com.aaspaas.aaspaas_backend.marketplace.entity.ServiceBooking;
import com.aaspaas.aaspaas_backend.marketplace.enums.BookingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Optional;

public interface ServiceBookingRepository extends JpaRepository<ServiceBooking, Long> {

    Page<ServiceBooking> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);

    Page<ServiceBooking> findByProviderIdOrderByCreatedAtDesc(Long providerId, Pageable pageable);

    Page<ServiceBooking> findByProviderIdAndStatusOrderByCreatedAtDesc(
            Long providerId, BookingStatus status, Pageable pageable);

    boolean existsByCustomerIdAndListingIdAndScheduledAtAndStatusIn(
            Long customerId, Long listingId, OffsetDateTime scheduledAt,
            Collection<BookingStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM ServiceBooking b WHERE b.id = :id")
    Optional<ServiceBooking> findByIdForUpdate(@Param("id") Long id);
}
