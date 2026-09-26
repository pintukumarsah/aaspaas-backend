package com.aaspaas.aaspaas_backend.delivery.repository;

import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryQuote;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DeliveryQuoteRepository
        extends JpaRepository<DeliveryQuote, Long> {

    List<DeliveryQuote> findByDeliveryRequestIdOrderByQuotedAmountAsc(
            Long deliveryRequestId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT q
        FROM DeliveryQuote q
        JOIN FETCH q.deliveryRequest dr
        JOIN FETCH q.partner p
        WHERE q.id = :quoteId
        """)
    Optional<DeliveryQuote> findByIdForUpdate(
            @Param("quoteId") Long quoteId
    );

    boolean existsByDeliveryRequestIdAndStatus(
            Long deliveryRequestId,
            String status
    );
}