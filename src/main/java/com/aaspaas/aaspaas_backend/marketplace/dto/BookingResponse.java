package com.aaspaas.aaspaas_backend.marketplace.dto;

import com.aaspaas.aaspaas_backend.marketplace.enums.BookingStatus;
import com.aaspaas.aaspaas_backend.marketplace.enums.PricingType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Builder
public class BookingResponse {
    private Long id;
    private String bookingNumber;
    private Long listingId;
    private String listingTitle;
    private Long customerId;
    private String customerName;
    private Long providerId;
    private String providerName;
    private OffsetDateTime scheduledAt;
    private String addressText;
    private String notes;
    private PricingType pricingType;
    private BigDecimal unitPrice;
    private Integer estimatedHours;
    private BigDecimal totalAmount;
    private BigDecimal commissionPercent;
    private BigDecimal commissionAmount;
    private BigDecimal providerEarning;
    private BookingStatus status;
    private String statusReason;
    private OffsetDateTime acceptedAt;
    private OffsetDateTime startedAt;
    private OffsetDateTime completedAt;
    private OffsetDateTime createdAt;
}
