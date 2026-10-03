package com.aaspaas.aaspaas_backend.cancellation.controller;

import com.aaspaas.aaspaas_backend.cancellation.dto.CancelOrderRequest;
import com.aaspaas.aaspaas_backend.cancellation.dto.CancellationResponse;
import com.aaspaas.aaspaas_backend.cancellation.service.OrderCancellationService;
import com.aaspaas.aaspaas_backend.common.dto.ApiResponse;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderCancellationController {

    private final OrderCancellationService
            orderCancellationService;

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<CancellationResponse>>
    cancelOrder(
            @PathVariable Long orderId,

            @Valid
            @RequestBody
            CancelOrderRequest request,

            Principal principal
    ) {

        CancellationResponse response =
                orderCancellationService.cancelOrder(
                        orderId,
                        request,
                        principal.getName()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Order cancelled successfully",
                        response
                )
        );
    }
}