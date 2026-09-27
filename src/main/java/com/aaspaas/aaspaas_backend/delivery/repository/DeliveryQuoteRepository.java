package com.aaspaas.aaspaas_backend.delivery.repository;

import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryQuote;
import com.aaspaas.aaspaas_backend.delivery.enums.DeliveryQuoteStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DeliveryQuoteRepository
        extends JpaRepository<DeliveryQuote, Long> {

    List<DeliveryQuote>
    findByDeliveryRequestIdOrderByQuotedAmountAsc(
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

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE DeliveryQuote q
        SET q.status = :cancelledStatus,
            q.updatedAt = CURRENT_TIMESTAMP
        WHERE q.deliveryRequest.id = :requestId
          AND q.id <> :selectedQuoteId
          AND q.status = :pendingStatus
        """)
    int cancelOtherPendingQuotes(
            @Param("requestId") Long requestId,
            @Param("selectedQuoteId") Long selectedQuoteId,
            @Param("pendingStatus")
            DeliveryQuoteStatus pendingStatus,
            @Param("cancelledStatus")
            DeliveryQuoteStatus cancelledStatus
    );
}