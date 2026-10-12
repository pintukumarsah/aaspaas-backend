package com.aaspaas.aaspaas_backend.infra.locationcache;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "false", matchIfMissing = true)
public class InMemoryLiveLocationCache implements LiveLocationCache {

    private final ConcurrentHashMap<Long, LocationPoint> points = new ConcurrentHashMap<>();

    @Value("${location-cache.ttl-seconds:120}")
    private long ttlSeconds;

    @Override
    public void put(Long assignmentId, LocationPoint point) {
        points.put(assignmentId, point);
    }

    @Override
    public Optional<LocationPoint> get(Long assignmentId) {
        LocationPoint p = points.get(assignmentId);
        if (p == null) {
            return Optional.empty();
        }
        if (p.recordedAt().plusSeconds(ttlSeconds).isBefore(OffsetDateTime.now())) {
            points.remove(assignmentId, p);
            return Optional.empty();
        }
        return Optional.of(p);
    }

    @Override
    public void evict(Long assignmentId) {
        points.remove(assignmentId);
    }
}
