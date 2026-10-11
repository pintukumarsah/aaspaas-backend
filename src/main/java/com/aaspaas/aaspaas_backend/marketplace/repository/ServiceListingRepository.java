package com.aaspaas.aaspaas_backend.marketplace.repository;

import com.aaspaas.aaspaas_backend.marketplace.entity.ServiceListing;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ServiceListingRepository
        extends JpaRepository<ServiceListing, Long>, JpaSpecificationExecutor<ServiceListing> {

    Page<ServiceListing> findByProviderIdOrderByCreatedAtDesc(Long providerId, Pageable pageable);

    long countByProviderId(Long providerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM ServiceListing l WHERE l.id = :id")
    Optional<ServiceListing> findByIdForUpdate(@Param("id") Long id);
}
