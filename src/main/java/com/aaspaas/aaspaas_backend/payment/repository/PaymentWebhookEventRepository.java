package com.aaspaas.aaspaas_backend.payment.repository;

import com.aaspaas.aaspaas_backend.payment.entity.PaymentWebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentWebhookEventRepository
        extends JpaRepository<PaymentWebhookEvent, Long> {

    Optional<PaymentWebhookEvent>
    findByProviderAndEventId(
            String provider,
            String eventId
    );

    boolean existsByProviderAndEventId(
            String provider,
            String eventId
    );
}