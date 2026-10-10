package com.aaspaas.aaspaas_backend.admin.reports.service;

import java.time.OffsetDateTime;
import java.util.Map;

public interface AdminReportsService {
    Map<String, Object> getOverview(OffsetDateTime from, OffsetDateTime to);
    Map<String, Object> getOrderReport(OffsetDateTime from, OffsetDateTime to);
    Map<String, Object> getDeliveryReport(OffsetDateTime from, OffsetDateTime to);
    Map<String, Object> getPayoutReport(OffsetDateTime from, OffsetDateTime to);
}
