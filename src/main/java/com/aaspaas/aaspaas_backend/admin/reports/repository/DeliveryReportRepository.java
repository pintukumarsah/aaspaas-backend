package com.aaspaas.aaspaas_backend.admin.reports.repository;

import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface DeliveryReportRepository extends JpaRepository<DeliveryRequest, Long> {

    @Query("""
        SELECT d.status, COUNT(d), COALESCE(SUM(d.maxBudget), 0)
        FROM DeliveryRequest d
        WHERE d.createdAt >= :from AND d.createdAt < :to
        GROUP BY d.status
        ORDER BY d.status
        """)
    List<Object[]> summarizeByStatus(@Param("from") OffsetDateTime from,
                                     @Param("to") OffsetDateTime to);

    @Query("""
        SELECT d.deliveryMode, COUNT(d)
        FROM DeliveryRequest d
        WHERE d.createdAt >= :from AND d.createdAt < :to
        GROUP BY d.deliveryMode
        ORDER BY d.deliveryMode
        """)
    List<Object[]> summarizeByMode(@Param("from") OffsetDateTime from,
                                   @Param("to") OffsetDateTime to);
}
