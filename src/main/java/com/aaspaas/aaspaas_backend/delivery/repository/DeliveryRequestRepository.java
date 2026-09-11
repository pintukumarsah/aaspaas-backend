package com.aaspaas.aaspaas_backend.delivery.repository;

import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryRequest;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DeliveryRequestRepository
        extends JpaRepository<DeliveryRequest, Long> {

    Optional<DeliveryRequest>
    findByOrderId(Long orderId);

    List<DeliveryRequest>
    findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    boolean existsByOrderId(Long orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT d
        FROM DeliveryRequest d
        WHERE d.id = :id
    """)
    Optional<DeliveryRequest>
    findByIdForUpdate(@Param("id") Long id);
}