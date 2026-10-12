package com.aaspaas.aaspaas_backend.order.dispute.controller;

import com.aaspaas.aaspaas_backend.common.dto.ApiResponse;
import com.aaspaas.aaspaas_backend.order.dispute.dto.DisputeRefundExecutionResponse;
import com.aaspaas.aaspaas_backend.order.dispute.service.DisputeRefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Covered by the existing /api/admin/** ADMIN rule. */
@RestController
@RequestMapping("/api/admin/disputes")
@RequiredArgsConstructor
public class AdminDisputeRefundController {

    private final DisputeRefundService disputeRefundService;

    @PostMapping("/{disputeId}/execute-refund")
    public ResponseEntity<ApiResponse<DisputeRefundExecutionResponse>> executeRefund(
            @PathVariable Long disputeId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Refund sent to the payment gateway. Final status arrives via webhook.",
                disputeRefundService.executeRefund(disputeId)));
    }
}
