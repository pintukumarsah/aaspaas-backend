package com.aaspaas.aaspaas_backend.order.dispute.repository;

import com.aaspaas.aaspaas_backend.order.dispute.entity.OrderDispute;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderDisputeRepository extends JpaRepository<OrderDispute, Long> {
    Page<OrderDispute> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<OrderDispute> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    boolean existsByOrderIdAndRaisedByIdAndStatusIn(Long orderId, Long raisedById, java.util.Collection<String> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from OrderDispute d where d.id = :id")
    Optional<OrderDispute> findByIdForUpdate(@Param("id") Long id);
}
