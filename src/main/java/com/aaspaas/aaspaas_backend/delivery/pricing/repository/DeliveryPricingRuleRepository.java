package com.aaspaas.aaspaas_backend.delivery.pricing.repository;

import com.aaspaas.aaspaas_backend.delivery.pricing.entity.DeliveryPricingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface DeliveryPricingRuleRepository
        extends JpaRepository<DeliveryPricingRule, Long> {

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
}