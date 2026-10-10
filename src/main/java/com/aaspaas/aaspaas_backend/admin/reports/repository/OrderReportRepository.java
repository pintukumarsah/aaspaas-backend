package com.aaspaas.aaspaas_backend.admin.reports.repository;

import com.aaspaas.aaspaas_backend.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface OrderReportRepository extends JpaRepository<Order, Long> {

    @Query("""
        SELECT o.orderStatus, COUNT(o), COALESCE(SUM(o.totalAmount), 0),
               COALESCE(SUM(o.deliveryFee), 0), COALESCE(SUM(o.platformFee), 0)
        FROM Order o
        WHERE o.createdAt >= :from AND o.createdAt < :to
        GROUP BY o.orderStatus
        ORDER BY o.orderStatus
        """)
    List<Object[]> summarizeByStatus(@Param("from") OffsetDateTime from,
                                     @Param("to") OffsetDateTime to);

    @Query("""
        SELECT COUNT(o), COALESCE(SUM(o.totalAmount), 0),
               COALESCE(SUM(o.subtotal), 0), COALESCE(SUM(o.deliveryFee), 0),
               COALESCE(SUM(o.platformFee), 0), COALESCE(SUM(o.discountAmount), 0)
        FROM Order o
        WHERE o.createdAt >= :from AND o.createdAt < :to
        """)
    Object[] summarizeTotals(@Param("from") OffsetDateTime from,
                             @Param("to") OffsetDateTime to);

    @Query("""
        SELECT o.paymentStatus, COUNT(o), COALESCE(SUM(o.totalAmount), 0)
        FROM Order o
        WHERE o.createdAt >= :from AND o.createdAt < :to
        GROUP BY o.paymentStatus
        ORDER BY o.paymentStatus
        """)
    List<Object[]> summarizeByPaymentStatus(@Param("from") OffsetDateTime from,
                                            @Param("to") OffsetDateTime to);
}
