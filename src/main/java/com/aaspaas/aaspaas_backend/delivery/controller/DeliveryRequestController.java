package com.aaspaas.aaspaas_backend.delivery.controller;

import com.aaspaas.aaspaas_backend.common.dto.ApiResponse;
import com.aaspaas.aaspaas_backend.delivery.dto.CreateDeliveryRequest;
import com.aaspaas.aaspaas_backend.delivery.dto.DeliveryRequestResponse;
import com.aaspaas.aaspaas_backend.delivery.service.DeliveryRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/delivery/requests")
@RequiredArgsConstructor
public class DeliveryRequestController {

    private final DeliveryRequestService
            deliveryRequestService;

    @PostMapping
    public ResponseEntity<ApiResponse<DeliveryRequestResponse>>
    createRequest(
            @Valid
            @RequestBody
            CreateDeliveryRequest request,
            Principal principal
    ) {

        DeliveryRequestResponse response =
                deliveryRequestService.createRequest(
                        request,
                        principal.getName()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Delivery request created successfully",
                                response
                        )
                );
    }

    @GetMapping
    public ResponseEntity<
            ApiResponse<List<DeliveryRequestResponse>>>
    getMyRequests(
            Principal principal
    ) {

        List<DeliveryRequestResponse> response =
                deliveryRequestService.getMyRequests(
                        principal.getName()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Delivery requests fetched successfully",
                        response
                )
        );
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ApiResponse<DeliveryRequestResponse>>
    getRequest(
            @PathVariable Long requestId,
            Principal principal
    ) {

        DeliveryRequestResponse response =
                deliveryRequestService.getRequest(
                        requestId,
                        principal.getName()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Delivery request fetched successfully",
                        response
                )
        );
    }

    @DeleteMapping("/{requestId}")
    public ResponseEntity<ApiResponse<Void>>
    cancelRequest(
            @PathVariable Long requestId,
            Principal principal
    ) {

        deliveryRequestService.cancelRequest(
                requestId,
                principal.getName()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Delivery request cancelled successfully",
                        null
                )
        );
    }
}