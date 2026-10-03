package com.aaspaas.aaspaas_backend.payment.repository;

import com.aaspaas.aaspaas_backend.payment.entity.PaymentRefund;
import com.aaspaas.aaspaas_backend.payment.enums.RefundStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentRefundRepository
        extends JpaRepository<PaymentRefund, Long> {

    Optional<PaymentRefund>
    findByPaymentId(Long paymentId);

    Optional<PaymentRefund>
    findByOrderId(Long orderId);

    Optional<PaymentRefund>
    findByGatewayRefundId(String gatewayRefundId);

    boolean existsByPaymentId(Long paymentId);

    boolean existsByOrderId(Long orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r
        FROM PaymentRefund r
        WHERE r.id = :refundId
        """)
    Optional<PaymentRefund> findByIdForUpdate(
            @Param("refundId") Long refundId
    );

    long countByStatus(RefundStatus status);
}