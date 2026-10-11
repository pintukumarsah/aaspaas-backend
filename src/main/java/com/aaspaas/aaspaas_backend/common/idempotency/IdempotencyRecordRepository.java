package com.aaspaas.aaspaas_backend.common.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyRecordRepository
        extends JpaRepository<IdempotencyRecord, Long> {

    Optional<IdempotencyRecord>
    findByCustomerPhoneAndEndpointAndIdempotencyKey(
            String customerPhone,
            String endpoint,
            String idempotencyKey
    );
}