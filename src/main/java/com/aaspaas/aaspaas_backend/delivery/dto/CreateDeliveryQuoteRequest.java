package com.aaspaas.aaspaas_backend.delivery.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateDeliveryQuoteRequest {

    private Long deliveryRequestId;

    private BigDecimal quotedAmount;

    private Integer estimatedMinutes;

    private String message;
}