package com.aaspaas.aaspaas_backend.cancellation.repository;

import com.aaspaas.aaspaas_backend.cancellation.entity.OrderCancellation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderCancellationRepository
        extends JpaRepository<OrderCancellation, Long> {

    Optional<OrderCancellation>
    findByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT c
        FROM OrderCancellation c
        WHERE c.order.id = :orderId
        """)
    Optional<OrderCancellation> findByOrderIdForUpdate(
            @Param("orderId") Long orderId
    );
}