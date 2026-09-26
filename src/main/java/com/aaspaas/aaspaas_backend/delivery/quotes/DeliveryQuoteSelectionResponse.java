package com.aaspaas.aaspaas_backend.delivery.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DeliveryQuoteSelectionResponse {

    private Long deliveryRequestId;

    private Long quoteId;

    private Long assignmentId;

    private Long partnerId;

    private BigDecimal agreedDeliveryFee;

    private BigDecimal platformCommission;

    private BigDecimal partnerEarning;

    private String currency;

    private String status;
}