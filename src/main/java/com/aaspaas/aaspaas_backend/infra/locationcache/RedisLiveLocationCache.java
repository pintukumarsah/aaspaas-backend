package com.aaspaas.aaspaas_backend.infra.locationcache;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * Stores the latest point as JSON with a TTL. Any Redis or JSON problem is logged
 * and treated as a cache miss, so callers fall back to the database.
 * Requires the JavaTimeModule on the ObjectMapper (Spring Boot registers it by default).
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true")
public class RedisLiveLocationCache implements LiveLocationCache {

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    @Value("${location-cache.ttl-seconds:120}")
    private long ttlSeconds;

    public RedisLiveLocationCache(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    @Override
    public void put(Long assignmentId, LocationPoint point) {
        try {
            redis.opsForValue().set(key(assignmentId),
                    objectMapper.writeValueAsString(point), Duration.ofSeconds(ttlSeconds));
        } catch (Exception ex) {
            log.warn("Live location cache write failed: {}", ex.getMessage());
        }
    }

    @Override
    public Optional<LocationPoint> get(Long assignmentId) {
        try {
            String json = redis.opsForValue().get(key(assignmentId));
            return json == null
                    ? Optional.empty()
                    : Optional.of(objectMapper.readValue(json, LocationPoint.class));
        } catch (Exception ex) {
            log.warn("Live location cache read failed: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void evict(Long assignmentId) {
        try {
            redis.delete(key(assignmentId));
        } catch (RuntimeException ex) {
            log.warn("Live location cache evict failed: {}", ex.getMessage());
        }
    }

    private String key(Long assignmentId) {
        return "loc:assignment:" + assignmentId;
    }
}
