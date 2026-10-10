package com.aaspaas.aaspaas_backend.admin.reports.controller;

import com.aaspaas.aaspaas_backend.admin.reports.service.AdminReportsService;
import com.aaspaas.aaspaas_backend.common.dto.ApiResponse;
import com.aaspaas.aaspaas_backend.common.exception.BusinessException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/reports")
public class AdminReportsController {
    private final AdminReportsService reportsService;

    public AdminReportsController(AdminReportsService reportsService) {
        this.reportsService = reportsService;
    }

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> overview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        Range range = validateRange(from, to);
        return ResponseEntity.ok(ApiResponse.success("Admin report generated",
                reportsService.getOverview(range.from(), range.to())));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<Map<String, Object>>> orders(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        Range range = validateRange(from, to);
        return ResponseEntity.ok(ApiResponse.success("Order report generated",
                reportsService.getOrderReport(range.from(), range.to())));
    }

    @GetMapping("/deliveries")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deliveries(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        Range range = validateRange(from, to);
        return ResponseEntity.ok(ApiResponse.success("Delivery report generated",
                reportsService.getDeliveryReport(range.from(), range.to())));
    }

    @GetMapping("/payouts")
    public ResponseEntity<ApiResponse<Map<String, Object>>> payouts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        Range range = validateRange(from, to);
        return ResponseEntity.ok(ApiResponse.success("Payout report generated",
                reportsService.getPayoutReport(range.from(), range.to())));
    }

    private Range validateRange(OffsetDateTime from, OffsetDateTime to) {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime effectiveTo = to == null ? now : to;
        OffsetDateTime effectiveFrom = from == null ? effectiveTo.minusDays(30) : from;
        if (!effectiveFrom.isBefore(effectiveTo)) {
            throw new BusinessException("'from' must be earlier than 'to'", 400);
        }
        if (effectiveFrom.isBefore(effectiveTo.minusDays(366))) {
            throw new BusinessException("Report range cannot exceed 366 days", 400);
        }
        return new Range(effectiveFrom, effectiveTo);
    }

    private record Range(OffsetDateTime from, OffsetDateTime to) { }
}
