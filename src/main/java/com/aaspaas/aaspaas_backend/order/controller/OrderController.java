package com.aaspaas.aaspaas_backend.order.controller;

import com.aaspaas.aaspaas_backend.common.dto.ApiResponse;
import com.aaspaas.aaspaas_backend.order.dto.CreateOrderRequest;
import com.aaspaas.aaspaas_backend.order.dto.OrderResponse;
import com.aaspaas.aaspaas_backend.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Principal principal
    ) {

        OrderResponse response =
                orderService.createOrder(
                        request,
                        principal.getName()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Order created successfully",
                                response
                        )
                );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(
            Principal principal
    ) {

        List<OrderResponse> response =
                orderService.getMyOrders(
                        principal.getName()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Orders fetched successfully",
                        response
                )
        );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getMyOrder(
            @PathVariable Long orderId,
            Principal principal
    ) {

        OrderResponse response =
                orderService.getMyOrder(
                        orderId,
                        principal.getName()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Order fetched successfully",
                        response
                )
        );
    }
}