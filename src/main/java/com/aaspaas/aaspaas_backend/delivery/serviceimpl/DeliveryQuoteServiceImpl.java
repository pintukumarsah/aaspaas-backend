package com.aaspaas.aaspaas_backend.delivery.serviceimpl;

import com.aaspaas.aaspaas_backend.delivery.dto.CreateDeliveryQuoteRequest;
import com.aaspaas.aaspaas_backend.delivery.dto.DeliveryQuoteResponse;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryPartner;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryQuote;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryRequest;
import com.aaspaas.aaspaas_backend.delivery.repository.DeliveryPartnerRepository;
import com.aaspaas.aaspaas_backend.delivery.repository.DeliveryQuoteRepository;
import com.aaspaas.aaspaas_backend.delivery.repository.DeliveryRequestRepository;
import com.aaspaas.aaspaas_backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.aaspaas.aaspaas_backend.delivery.service.DeliveryQuoteService;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryQuoteServiceImpl
        implements DeliveryQuoteService {

    private final DeliveryQuoteRepository quoteRepository;
    private final DeliveryRequestRepository requestRepository;
    private final DeliveryPartnerRepository partnerRepository;

    @Override
    public DeliveryQuoteResponse createQuote(
            CreateDeliveryQuoteRequest request) {

        String phone = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        DeliveryPartner partner =
                partnerRepository.findByUserPhone(phone)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Delivery partner profile not found"
                                ));

        if (!"ONLINE".equals(partner.getAvailabilityStatus())) {
            throw new RuntimeException(
                    "Delivery partner must be ONLINE"
            );
        }

        DeliveryRequest deliveryRequest =
                requestRepository.findById(
                        request.getDeliveryRequestId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Delivery request not found"
                        ));

        if (!"OPEN".equals(deliveryRequest.getStatus())) {
            throw new RuntimeException(
                    "Delivery request is no longer open"
            );
        }

        if (deliveryRequest.getExpiresAt() != null &&
            deliveryRequest.getExpiresAt()
                    .isBefore(java.time.OffsetDateTime.now())) {

            throw new RuntimeException(
                    "Delivery request has expired"
            );
        }

        if (request.getQuotedAmount() == null ||
            request.getQuotedAmount()
                    .compareTo(BigDecimal.ZERO) <= 0) {

            throw new RuntimeException(
                    "Quote amount must be greater than zero"
            );
        }

        if (deliveryRequest.getMaxBudget() != null &&
            deliveryRequest.getMaxBudget()
                    .compareTo(BigDecimal.ZERO) > 0 &&
            request.getQuotedAmount()
                    .compareTo(deliveryRequest.getMaxBudget()) > 0) {

            throw new RuntimeException(
                    "Quote amount exceeds customer maximum budget"
            );
        }

        if (quoteRepository
                .findByDeliveryRequestIdAndPartnerId(
                        deliveryRequest.getId(),
                        partner.getId()
                ).isPresent()) {

            throw new RuntimeException(
                    "You have already submitted a quote"
            );
        }

        DeliveryQuote quote = DeliveryQuote.builder()
                .deliveryRequest(deliveryRequest)
                .partner(partner)
                .quotedAmount(request.getQuotedAmount())
                .estimatedMinutes(request.getEstimatedMinutes())
                .message(request.getMessage())
                .status("PENDING")
                .build();

        quote = quoteRepository.save(quote);

        return mapToResponse(quote);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryQuoteResponse> getQuotesForRequest(
            Long deliveryRequestId) {

        return quoteRepository
                .findByDeliveryRequestIdOrderByQuotedAmountAsc(
                        deliveryRequestId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryQuoteResponse> getMyQuotes() {

        String phone = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        DeliveryPartner partner =
                partnerRepository.findByUserPhone(phone)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Delivery partner profile not found"
                                ));

        return quoteRepository
                .findByPartnerIdOrderByCreatedAtDesc(
                        partner.getId()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private DeliveryQuoteResponse mapToResponse(
            DeliveryQuote quote) {

        User user = quote.getPartner().getUser();

        return DeliveryQuoteResponse.builder()
                .id(quote.getId())
                .deliveryRequestId(
                        quote.getDeliveryRequest().getId()
                )
                .partnerId(
                        quote.getPartner().getId()
                )
                .userId(user.getId())
                .partnerName(user.getFullName())
                .quotedAmount(quote.getQuotedAmount())
                .estimatedMinutes(
                        quote.getEstimatedMinutes()
                )
                .message(quote.getMessage())
                .status(quote.getStatus())
                .createdAt(quote.getCreatedAt())
                .build();
    }
}