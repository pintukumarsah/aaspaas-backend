package com.aaspaas.aaspaas_backend.order.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
public class OrderResponse {

    private Long id;

    private String orderNumber;

    private Long customerId;

    private Long businessId;

    private String businessName;

    private Long deliveryAddressId;

    private BigDecimal subtotal;

    private BigDecimal deliveryFee;

    private BigDecimal platformFee;

    private BigDecimal discountAmount;

    private BigDecimal totalAmount;

    private String paymentStatus;

    private String orderStatus;

    private List<OrderItemResponse> items;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}