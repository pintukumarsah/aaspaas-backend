package com.aaspaas.aaspaas_backend.marketplace.dto;

import com.aaspaas.aaspaas_backend.marketplace.enums.ListingStatus;
import com.aaspaas.aaspaas_backend.marketplace.enums.PricingType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Builder
public class ListingResponse {
    private Long id;
    private Long providerId;
    private String providerName;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String description;
    private PricingType pricingType;
    private BigDecimal basePrice;
    private String city;
    private String locality;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private ListingStatus status;
    private OffsetDateTime createdAt;
}
