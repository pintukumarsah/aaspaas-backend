package com.aaspaas.aaspaas_backend.commission.service;

import com.aaspaas.aaspaas_backend.commission.dto.CommissionResponse;
import com.aaspaas.aaspaas_backend.commission.dto.CreateCommissionRequest;

import java.time.OffsetDateTime;
import java.util.List;

public interface CommissionService {

    CommissionResponse createCommission(
            CreateCommissionRequest request
    );

    CommissionResponse getCommission(
            Long commissionId
    );

    List<CommissionResponse> getOrderCommissions(
            Long orderId
    );

    List<CommissionResponse> getBusinessCommissions(
            Long businessId,
            OffsetDateTime startDate,
            OffsetDateTime endDate
    );

    CommissionResponse reverseCommission(
            Long commissionId,
            String reason
    );
}