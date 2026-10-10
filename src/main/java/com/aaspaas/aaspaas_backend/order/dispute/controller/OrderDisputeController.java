package com.aaspaas.aaspaas_backend.order.dispute.controller;

import com.aaspaas.aaspaas_backend.common.dto.ApiResponse;
import com.aaspaas.aaspaas_backend.order.dispute.dto.CreateOrderDisputeRequest;
import com.aaspaas.aaspaas_backend.order.dispute.dto.OrderDisputeResponse;
import com.aaspaas.aaspaas_backend.order.dispute.service.OrderDisputeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/orders/{orderId}/disputes")
@RequiredArgsConstructor
public class OrderDisputeController {
    private final OrderDisputeService disputeService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDisputeResponse>> raiseDispute(
            @PathVariable Long orderId,
            @Valid @RequestBody CreateOrderDisputeRequest request,
            Principal principal) {
        OrderDisputeResponse response = disputeService.raiseDispute(orderId, principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Order dispute submitted for review. No refund has been processed by this endpoint.", response));
    }
}
