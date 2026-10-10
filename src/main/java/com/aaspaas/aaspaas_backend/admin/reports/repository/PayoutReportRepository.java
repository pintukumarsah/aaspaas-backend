package com.aaspaas.aaspaas_backend.admin.reports.repository;

import com.aaspaas.aaspaas_backend.delivery.entity.PartnerPayout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface PayoutReportRepository extends JpaRepository<PartnerPayout, Long> {

    @Query("""
        SELECT p.status, COUNT(p), COALESCE(SUM(p.grossAmount), 0),
               COALESCE(SUM(p.platformFee), 0), COALESCE(SUM(p.netAmount), 0)
        FROM PartnerPayout p
        WHERE p.createdAt >= :from AND p.createdAt < :to
        GROUP BY p.status
        ORDER BY p.status
        """)
    List<Object[]> summarizeByStatus(@Param("from") OffsetDateTime from,
                                     @Param("to") OffsetDateTime to);
}
