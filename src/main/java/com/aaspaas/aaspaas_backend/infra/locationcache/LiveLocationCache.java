package com.aaspaas.aaspaas_backend.infra.locationcache;

import java.util.Optional;

/** Latest known partner position per delivery assignment. PostgreSQL stays the source of truth. */
public interface LiveLocationCache {

    void put(Long assignmentId, LocationPoint point);

    Optional<LocationPoint> get(Long assignmentId);

    void evict(Long assignmentId);
}
