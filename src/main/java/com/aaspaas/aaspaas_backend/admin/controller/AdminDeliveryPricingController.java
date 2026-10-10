package com.aaspaas.aaspaas_backend.admin.controller;

import com.aaspaas.aaspaas_backend.admin.dto.DeliveryPricingRuleRequest;
import com.aaspaas.aaspaas_backend.admin.dto.DeliveryPricingRuleResponse;
import com.aaspaas.aaspaas_backend.admin.service.DeliveryPricingAdminService;
import com.aaspaas.aaspaas_backend.common.dto.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/delivery-pricing-rules")
@RequiredArgsConstructor
@Validated
public class AdminDeliveryPricingController {
    private final DeliveryPricingAdminService adminService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<DeliveryPricingRuleResponse>>> listRules(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String deliveryMode,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(ApiResponse.success("Delivery pricing rules retrieved", adminService.listRules(active, deliveryMode, page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeliveryPricingRuleResponse>> getRule(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Delivery pricing rule retrieved", adminService.getRule(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DeliveryPricingRuleResponse>> createRule(@Valid @RequestBody DeliveryPricingRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Delivery pricing rule created", adminService.createRule(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DeliveryPricingRuleResponse>> updateRule(@PathVariable Long id, @Valid @RequestBody DeliveryPricingRuleRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Delivery pricing rule updated", adminService.updateRule(id, request)));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<DeliveryPricingRuleResponse>> activateRule(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Delivery pricing rule activated", adminService.setActive(id, true)));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<DeliveryPricingRuleResponse>> deactivateRule(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Delivery pricing rule deactivated", adminService.setActive(id, false)));
    }
}
