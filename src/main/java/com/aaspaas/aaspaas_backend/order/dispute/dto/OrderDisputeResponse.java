package com.aaspaas.aaspaas_backend.order.dispute.dto;

import com.aaspaas.aaspaas_backend.order.dispute.entity.OrderDispute;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
public class OrderDisputeResponse {
    private Long id;
    private Long orderId;
    private String orderNumber;
    private Long raisedByUserId;
    private String reasonCode;
    private String description;
    private BigDecimal requestedRefundAmount;
    private BigDecimal approvedRefundAmount;
    private String currency;
    private String status;
    private String resolutionCode;
    private String resolutionNotes;
    private Long reviewedByUserId;
    private OffsetDateTime reviewedAt;
    private OffsetDateTime resolvedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static OrderDisputeResponse fromEntity(OrderDispute entity) {
        OrderDisputeResponse response = new OrderDisputeResponse();
        response.setId(entity.getId());
        response.setOrderId(entity.getOrder().getId());
        response.setOrderNumber(entity.getOrder().getOrderNumber());
        response.setRaisedByUserId(entity.getRaisedBy().getId());
        response.setReasonCode(entity.getReasonCode());
        response.setDescription(entity.getDescription());
        response.setRequestedRefundAmount(entity.getRequestedRefundAmount());
        response.setApprovedRefundAmount(entity.getApprovedRefundAmount());
        response.setCurrency(entity.getCurrency());
        response.setStatus(entity.getStatus());
        response.setResolutionCode(entity.getResolutionCode());
        response.setResolutionNotes(entity.getResolutionNotes());
        response.setReviewedByUserId(entity.getReviewedByUserId());
        response.setReviewedAt(entity.getReviewedAt());
        response.setResolvedAt(entity.getResolvedAt());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}
