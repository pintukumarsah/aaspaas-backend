package com.aaspaas.aaspaas_backend.admin.reports.serviceimpl;

import com.aaspaas.aaspaas_backend.admin.reports.repository.DeliveryReportRepository;
import com.aaspaas.aaspaas_backend.admin.reports.repository.OrderReportRepository;
import com.aaspaas.aaspaas_backend.admin.reports.repository.PayoutReportRepository;
import com.aaspaas.aaspaas_backend.admin.reports.service.AdminReportsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class AdminReportsServiceImpl implements AdminReportsService {
    private final OrderReportRepository orderRepository;
    private final DeliveryReportRepository deliveryRepository;
    private final PayoutReportRepository payoutRepository;

    public AdminReportsServiceImpl(OrderReportRepository orderRepository,
                                   DeliveryReportRepository deliveryRepository,
                                   PayoutReportRepository payoutRepository) {
        this.orderRepository = orderRepository;
        this.deliveryRepository = deliveryRepository;
        this.payoutRepository = payoutRepository;
    }

    @Override
    public Map<String, Object> getOverview(OffsetDateTime from, OffsetDateTime to) {
        Object[] totals = orderRepository.summarizeTotals(from, to);
        Map<String, Object> result = range(from, to);
        Map<String, Object> orders = new LinkedHashMap<>();
        orders.put("count", number(totals[0]));
        orders.put("grossOrderValue", decimal(totals[1]));
        orders.put("subtotal", decimal(totals[2]));
        orders.put("deliveryFees", decimal(totals[3]));
        orders.put("platformFees", decimal(totals[4]));
        orders.put("discounts", decimal(totals[5]));
        result.put("orders", orders);
        result.put("orderStatuses", mapOrderStatuses(from, to));
        result.put("paymentStatuses", mapPaymentStatuses(from, to));
        result.put("deliveryStatuses", mapDeliveryStatuses(from, to));
        result.put("payoutStatuses", mapPayoutStatuses(from, to));
        return result;
    }

    @Override
    public Map<String, Object> getOrderReport(OffsetDateTime from, OffsetDateTime to) {
        Map<String, Object> result = range(from, to);
        result.put("orderStatuses", mapOrderStatuses(from, to));
        result.put("paymentStatuses", mapPaymentStatuses(from, to));
        Object[] totals = orderRepository.summarizeTotals(from, to);
        result.put("totals", Map.of(
                "count", number(totals[0]),
                "grossOrderValue", decimal(totals[1]),
                "subtotal", decimal(totals[2]),
                "deliveryFees", decimal(totals[3]),
                "platformFees", decimal(totals[4]),
                "discounts", decimal(totals[5])));
        return result;
    }

    @Override
    public Map<String, Object> getDeliveryReport(OffsetDateTime from, OffsetDateTime to) {
        Map<String, Object> result = range(from, to);
        result.put("deliveryStatuses", mapDeliveryStatuses(from, to));
        List<Map<String, Object>> modes = new ArrayList<>();
        for (Object[] row : deliveryRepository.summarizeByMode(from, to)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("deliveryMode", row[0]);
            item.put("count", number(row[1]));
            modes.add(item);
        }
        result.put("deliveryModes", modes);
        return result;
    }

    @Override
    public Map<String, Object> getPayoutReport(OffsetDateTime from, OffsetDateTime to) {
        Map<String, Object> result = range(from, to);
        result.put("payoutStatuses", mapPayoutStatuses(from, to));
        return result;
    }

    private List<Map<String, Object>> mapOrderStatuses(OffsetDateTime from, OffsetDateTime to) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Object[] row : orderRepository.summarizeByStatus(from, to)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("status", row[0]);
            item.put("count", number(row[1]));
            item.put("grossOrderValue", decimal(row[2]));
            item.put("deliveryFees", decimal(row[3]));
            item.put("platformFees", decimal(row[4]));
            rows.add(item);
        }
        return rows;
    }

    private List<Map<String, Object>> mapPaymentStatuses(OffsetDateTime from, OffsetDateTime to) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Object[] row : orderRepository.summarizeByPaymentStatus(from, to)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("paymentStatus", row[0]);
            item.put("count", number(row[1]));
            item.put("orderValue", decimal(row[2]));
            rows.add(item);
        }
        return rows;
    }

    private List<Map<String, Object>> mapDeliveryStatuses(OffsetDateTime from, OffsetDateTime to) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Object[] row : deliveryRepository.summarizeByStatus(from, to)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("status", row[0]);
            item.put("count", number(row[1]));
            item.put("requestedBudgetTotal", decimal(row[2]));
            rows.add(item);
        }
        return rows;
    }

    private List<Map<String, Object>> mapPayoutStatuses(OffsetDateTime from, OffsetDateTime to) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Object[] row : payoutRepository.summarizeByStatus(from, to)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("status", row[0] == null ? null : row[0].toString());
            item.put("count", number(row[1]));
            item.put("grossAmount", decimal(row[2]));
            item.put("platformFee", decimal(row[3]));
            item.put("netAmount", decimal(row[4]));
            rows.add(item);
        }
        return rows;
    }

    private Map<String, Object> range(OffsetDateTime from, OffsetDateTime to) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("from", from);
        result.put("toExclusive", to);
        result.put("currency", "INR");
        return result;
    }

    private long number(Object value) {
        return value instanceof Number n ? n.longValue() : 0L;
    }

    private BigDecimal decimal(Object value) {
        if (value instanceof BigDecimal amount) return amount;
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return BigDecimal.ZERO;
    }
}
