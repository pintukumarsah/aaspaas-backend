package com.aaspaas.aaspaas_backend.delivery.pricing.repository;

import com.aaspaas.aaspaas_backend.delivery.pricing.entity.DeliveryPricingRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface DeliveryPricingRuleRepository extends JpaRepository<DeliveryPricingRule, Long> {

    @Query("""
        SELECT r
        FROM DeliveryPricingRule r
        WHERE r.deliveryMode = :deliveryMode
          AND r.active = true
          AND r.effectiveFrom <= :now
          AND (r.effectiveTo IS NULL OR r.effectiveTo > :now)
        ORDER BY r.effectiveFrom DESC
        """)
    Optional<DeliveryPricingRule> findActiveRule(
            @Param("deliveryMode") String deliveryMode,
            @Param("now") OffsetDateTime now
    );

    Page<DeliveryPricingRule> findByActive(boolean active, Pageable pageable);

    Page<DeliveryPricingRule> findByDeliveryMode(String deliveryMode, Pageable pageable);

    Page<DeliveryPricingRule> findByActiveAndDeliveryMode(boolean active, String deliveryMode, Pageable pageable);

    @Query("""
        SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
        FROM DeliveryPricingRule r
        WHERE r.active = true
          AND r.deliveryMode = :deliveryMode
          AND (:excludeId IS NULL OR r.id <> :excludeId)
          AND (
                (:candidateTo IS NULL AND (r.effectiveTo IS NULL OR r.effectiveTo > :candidateFrom))
                OR
                (:candidateTo IS NOT NULL AND r.effectiveFrom < :candidateTo
                    AND (r.effectiveTo IS NULL OR r.effectiveTo > :candidateFrom))
          )
        """)
    boolean existsOverlappingActiveRule(
            @Param("deliveryMode") String deliveryMode,
            @Param("candidateFrom") OffsetDateTime candidateFrom,
            @Param("candidateTo") OffsetDateTime candidateTo,
            @Param("excludeId") Long excludeId
    );
}
