package com.aaspaas.aaspaas_backend.order.dispute.controller;

import com.aaspaas.aaspaas_backend.common.dto.ApiResponse;
import com.aaspaas.aaspaas_backend.order.dispute.dto.OrderDisputeResponse;
import com.aaspaas.aaspaas_backend.order.dispute.dto.ResolveOrderDisputeRequest;
import com.aaspaas.aaspaas_backend.order.dispute.service.OrderDisputeService;
import com.aaspaas.aaspaas_backend.user.entity.User;
import com.aaspaas.aaspaas_backend.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/disputes")
@RequiredArgsConstructor
public class AdminOrderDisputeController {
    private final OrderDisputeService disputeService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderDisputeResponse>>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("Disputes fetched successfully", disputeService.getAdminDisputes(status, page, size)));
    }

    @GetMapping("/{disputeId}")
    public ResponseEntity<ApiResponse<OrderDisputeResponse>> get(@PathVariable Long disputeId) {
        return ResponseEntity.ok(ApiResponse.success("Dispute fetched successfully", disputeService.getAdminDispute(disputeId)));
    }

    @PatchMapping("/{disputeId}/review")
    public ResponseEntity<ApiResponse<OrderDisputeResponse>> review(@PathVariable Long disputeId, Principal principal) {
        User admin = getAuthenticatedUser(principal);
        return ResponseEntity.ok(ApiResponse.success("Dispute moved to review", disputeService.startReview(disputeId, admin.getId())));
    }

    @PatchMapping("/{disputeId}/resolve")
    public ResponseEntity<ApiResponse<OrderDisputeResponse>> resolve(
            @PathVariable Long disputeId,
            @Valid @RequestBody ResolveOrderDisputeRequest request,
            Principal principal) {
        User admin = getAuthenticatedUser(principal);
        return ResponseEntity.ok(ApiResponse.success("Dispute decision recorded. This does not execute a payment gateway refund.", disputeService.resolve(disputeId, admin.getId(), request)));
    }

    @PatchMapping("/{disputeId}/reject")
    public ResponseEntity<ApiResponse<OrderDisputeResponse>> reject(
            @PathVariable Long disputeId,
            @RequestBody Map<String, String> request,
            Principal principal) {
        User admin = getAuthenticatedUser(principal);
        return ResponseEntity.ok(ApiResponse.success("Dispute rejected", disputeService.reject(disputeId, admin.getId(), request.get("notes"))));
    }

    private User getAuthenticatedUser(Principal principal) {
        if (principal == null || principal.getName() == null) throw new com.aaspaas.aaspaas_backend.common.exception.BusinessException("Authenticated admin is required", 401);
        return userRepository.findByPhone(principal.getName())
                .orElseThrow(() -> new com.aaspaas.aaspaas_backend.common.exception.BusinessException("Authenticated admin user was not found", 404));
    }
}
