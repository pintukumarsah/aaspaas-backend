package com.aaspaas.aaspaas_backend.order.service;

import com.aaspaas.aaspaas_backend.order.dto.CreateOrderRequest;
import com.aaspaas.aaspaas_backend.order.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(
            CreateOrderRequest request,
            String authenticatedPhone
    );

    OrderResponse getMyOrder(
            Long orderId,
            String authenticatedPhone
    );

    List<OrderResponse> getMyOrders(
            String authenticatedPhone
    );
}