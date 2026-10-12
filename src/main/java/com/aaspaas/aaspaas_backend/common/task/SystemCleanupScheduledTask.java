package com.aaspaas.aaspaas_backend.common.task;

import com.aaspaas.aaspaas_backend.common.idempotency.IdempotencyRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class SystemCleanupScheduledTask {

    private final IdempotencyRecordRepository idempotencyRecordRepository;

    /**
     * Har roz raat ko 2:00 AM chalega.
     * 7 din se purane Idempotency records ko purge karega.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupOldIdempotencyRecords() {
        OffsetDateTime retentionCutoff = OffsetDateTime.now().minusDays(7);
        log.info("Starting cleanup for Idempotency records older than {}", retentionCutoff);
        
        try {
            idempotencyRecordRepository.findAll().stream()
                    .filter(r -> r.getCreatedAt() != null && r.getCreatedAt().isBefore(retentionCutoff))
                    .forEach(idempotencyRecordRepository::delete);
            log.info("Completed Idempotency records cleanup successfully.");
        } catch (Exception e) {
            log.error("Failed to clean up idempotency records: {}", e.getMessage());
        }
    }
}