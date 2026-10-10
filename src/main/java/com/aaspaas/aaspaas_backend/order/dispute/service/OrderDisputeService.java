package com.aaspaas.aaspaas_backend.order.dispute.service;

import com.aaspaas.aaspaas_backend.order.dispute.dto.CreateOrderDisputeRequest;
import com.aaspaas.aaspaas_backend.order.dispute.dto.OrderDisputeResponse;
import com.aaspaas.aaspaas_backend.order.dispute.dto.ResolveOrderDisputeRequest;
import org.springframework.data.domain.Page;

public interface OrderDisputeService {
    OrderDisputeResponse raiseDispute(Long orderId, String authenticatedPhone, CreateOrderDisputeRequest request);
    Page<OrderDisputeResponse> getAdminDisputes(String status, int page, int size);
    OrderDisputeResponse getAdminDispute(Long disputeId);
    OrderDisputeResponse startReview(Long disputeId, Long adminUserId);
    OrderDisputeResponse resolve(Long disputeId, Long adminUserId, ResolveOrderDisputeRequest request);
    OrderDisputeResponse reject(Long disputeId, Long adminUserId, String notes);
}
