package com.aaspaas.aaspaas_backend.delivery.service;

import com.aaspaas.aaspaas_backend.delivery.dto.DeliveryQuoteSelectionResponse;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryQuote;

import java.util.List;

public interface DeliveryQuoteService {

    DeliveryQuote createQuote(
            Long deliveryRequestId,
            Long partnerId,
            java.math.BigDecimal quotedAmount,
            Integer estimatedMinutes,
            String message
    );

    List<DeliveryQuote> getQuotesForRequest(
            Long deliveryRequestId
    );

    DeliveryQuoteSelectionResponse selectQuote(
            Long quoteId
    );
}