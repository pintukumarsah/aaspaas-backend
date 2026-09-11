package com.aaspaas.aaspaas_backend.delivery.repository;

import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryQuote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface DeliveryQuoteRepository
        extends JpaRepository<DeliveryQuote, Long> {

    List<DeliveryQuote>
    findByDeliveryRequestIdOrderByQuotedAmountAsc(
            Long deliveryRequestId
    );

    List<DeliveryQuote>
    findByPartnerIdOrderByCreatedAtDesc(
            Long partnerId
    );

    Optional<DeliveryQuote>
    findByDeliveryRequestIdAndPartnerId(
            Long deliveryRequestId,
            Long partnerId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("""
    SELECT q
    FROM DeliveryQuote q
    WHERE q.id = :id
""")
Optional<DeliveryQuote> findByIdForUpdate(
        @Param("id") Long id
);
}