package com.aaspaas.aaspaas_backend.commission.controller;

import com.aaspaas.aaspaas_backend.commission.dto.CommissionResponse;
import com.aaspaas.aaspaas_backend.commission.dto.CreateCommissionRequest;
import com.aaspaas.aaspaas_backend.commission.service.CommissionService;
import com.aaspaas.aaspaas_backend.common.dto.ApiResponse;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/commissions")
@RequiredArgsConstructor
public class CommissionController {

    private final CommissionService commissionService;

    @PostMapping
    public ResponseEntity<ApiResponse<CommissionResponse>>
    createCommission(
            @Valid
            @RequestBody
            CreateCommissionRequest request
    ) {

        CommissionResponse response =
                commissionService.createCommission(
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Commission created successfully",
                        response
                )
        );
    }

    @GetMapping("/{commissionId}")
    public ResponseEntity<ApiResponse<CommissionResponse>>
    getCommission(
            @PathVariable
            Long commissionId
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Commission fetched successfully",
                        commissionService.getCommission(
                                commissionId
                        )
                )
        );
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<List<CommissionResponse>>>
    getOrderCommissions(
            @PathVariable
            Long orderId
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Order commissions fetched successfully",
                        commissionService.getOrderCommissions(
                                orderId
                        )
                )
        );
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<ApiResponse<List<CommissionResponse>>>
    getBusinessCommissions(
            @PathVariable
            Long businessId,

            @RequestParam
            OffsetDateTime startDate,

            @RequestParam
            OffsetDateTime endDate
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Business commissions fetched successfully",
                        commissionService.getBusinessCommissions(
                                businessId,
                                startDate,
                                endDate
                        )
                )
        );
    }

    @PostMapping("/{commissionId}/reverse")
    public ResponseEntity<ApiResponse<CommissionResponse>>
    reverseCommission(
            @PathVariable
            Long commissionId,

            @RequestParam(required = false)
            String reason
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Commission reversed successfully",
                        commissionService.reverseCommission(
                                commissionId,
                                reason
                        )
                )
        );
    }
}