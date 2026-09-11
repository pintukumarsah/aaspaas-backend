package com.aaspaas.aaspaas_backend.delivery.controller;

import com.aaspaas.aaspaas_backend.delivery.dto.CreateDeliveryQuoteRequest;
import com.aaspaas.aaspaas_backend.delivery.dto.DeliveryQuoteResponse;
import com.aaspaas.aaspaas_backend.delivery.service.DeliveryQuoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery/quotes")
@RequiredArgsConstructor
public class DeliveryQuoteController {

    private final DeliveryQuoteService deliveryQuoteService;

    @PostMapping
    public ResponseEntity<DeliveryQuoteResponse> createQuote(
            @RequestBody CreateDeliveryQuoteRequest request) {

        return ResponseEntity.ok(
                deliveryQuoteService.createQuote(request)
        );
    }

    @GetMapping("/request/{deliveryRequestId}")
    public ResponseEntity<List<DeliveryQuoteResponse>>
    getQuotesForRequest(
            @PathVariable Long deliveryRequestId) {

        return ResponseEntity.ok(
                deliveryQuoteService.getQuotesForRequest(
                        deliveryRequestId
                )
        );
    }

    @GetMapping("/my")
    public ResponseEntity<List<DeliveryQuoteResponse>>
    getMyQuotes() {

        return ResponseEntity.ok(
                deliveryQuoteService.getMyQuotes()
        );
    }
}