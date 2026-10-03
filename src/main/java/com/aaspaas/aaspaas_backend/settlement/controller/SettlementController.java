package com.aaspaas.aaspaas_backend.settlement.controller;

import com.aaspaas.aaspaas_backend.common.dto.ApiResponse;
import com.aaspaas.aaspaas_backend.settlement.dto.PartnerPayoutBatchResponse;
import com.aaspaas.aaspaas_backend.settlement.dto.SellerSettlementResponse;
import com.aaspaas.aaspaas_backend.settlement.service.SettlementService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @PostMapping("/seller/{businessId}")
    public ResponseEntity<ApiResponse<SellerSettlementResponse>>
    createSellerSettlement(
            @PathVariable
            Long businessId,

            @RequestParam
            OffsetDateTime periodStart,

            @RequestParam
            OffsetDateTime periodEnd
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Seller settlement created successfully",
                        settlementService.createSellerSettlement(
                                businessId,
                                periodStart,
                                periodEnd
                        )
                )
        );
    }

    @GetMapping("/seller/{settlementId}")
    public ResponseEntity<ApiResponse<SellerSettlementResponse>>
    getSellerSettlement(
            @PathVariable
            Long settlementId
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Seller settlement fetched successfully",
                        settlementService.getSellerSettlement(
                                settlementId
                        )
                )
        );
    }

    @GetMapping("/seller/business/{businessId}")
    public ResponseEntity<ApiResponse<List<SellerSettlementResponse>>>
    getBusinessSettlements(
            @PathVariable
            Long businessId
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Seller settlements fetched successfully",
                        settlementService
                                .getBusinessSettlements(
                                        businessId
                                )
                )
        );
    }

    @PostMapping("/seller/{settlementId}/process")
    public ResponseEntity<ApiResponse<SellerSettlementResponse>>
    processSellerSettlement(
            @PathVariable
            Long settlementId
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Seller settlement processed successfully",
                        settlementService
                                .processSellerSettlement(
                                        settlementId
                                )
                )
        );
    }

    @PostMapping("/partner-payout/batch")
    public ResponseEntity<ApiResponse<PartnerPayoutBatchResponse>>
    createPartnerPayoutBatch() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Partner payout batch created successfully",
                        settlementService
                                .createPartnerPayoutBatch()
                )
        );
    }

    @GetMapping("/partner-payout/batch/{batchId}")
    public ResponseEntity<ApiResponse<PartnerPayoutBatchResponse>>
    getPartnerPayoutBatch(
            @PathVariable
            Long batchId
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Partner payout batch fetched successfully",
                        settlementService
                                .getPartnerPayoutBatch(
                                        batchId
                                )
                )
        );
    }

    @PostMapping(
            "/partner-payout/batch/{batchId}/process"
    )
    public ResponseEntity<ApiResponse<PartnerPayoutBatchResponse>>
    processPartnerPayoutBatch(
            @PathVariable
            Long batchId
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Partner payout batch moved to processing",
                        settlementService
                                .processPartnerPayoutBatch(
                                        batchId
                                )
                )
        );
    }
}