package com.aaspaas.aaspaas_backend.delivery.service;

import com.aaspaas.aaspaas_backend.delivery.dto.CreateDeliveryQuoteRequest;
import com.aaspaas.aaspaas_backend.delivery.dto.DeliveryQuoteResponse;

import java.util.List;

public interface DeliveryQuoteService {

    DeliveryQuoteResponse createQuote(
            CreateDeliveryQuoteRequest request
    );

    List<DeliveryQuoteResponse> getQuotesForRequest(
            Long deliveryRequestId
    );

    List<DeliveryQuoteResponse> getMyQuotes();
}