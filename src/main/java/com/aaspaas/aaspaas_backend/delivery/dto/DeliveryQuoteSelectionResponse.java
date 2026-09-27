package com.aaspaas.aaspaas_backend.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class DeliveryQuoteSelectionResponse {

    private Long deliveryRequestId;

    private Long quoteId;

    private Long assignmentId;

    private Long partnerId;

    private BigDecimal agreedDeliveryFee;

    private BigDecimal platformCommission;

    private BigDecimal partnerEarning;

    private String currency;

    private String assignmentStatus;
}