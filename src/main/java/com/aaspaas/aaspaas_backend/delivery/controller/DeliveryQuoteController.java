package com.aaspaas.aaspaas_backend.delivery.controller;

import com.aaspaas.aaspaas_backend.delivery.dto.DeliveryQuoteSelectionResponse;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryQuote;
import com.aaspaas.aaspaas_backend.delivery.service.DeliveryQuoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery/quotes")
@RequiredArgsConstructor
public class DeliveryQuoteController {

    private final DeliveryQuoteService deliveryQuoteService;


    @GetMapping("/request/{deliveryRequestId}")
    public ResponseEntity<List<DeliveryQuote>> getQuotes(
            @PathVariable Long deliveryRequestId
    ) {

        return ResponseEntity.ok(
                deliveryQuoteService
                        .getQuotesForRequest(
                                deliveryRequestId
                        )
        );
    }


    @PostMapping("/{quoteId}/select")
    public ResponseEntity<DeliveryQuoteSelectionResponse>
    selectQuote(
            @PathVariable Long quoteId
    ) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        deliveryQuoteService
                                .selectQuote(quoteId)
                );
    }
}