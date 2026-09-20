package com.aaspaas.aaspaas_backend.delivery.service;

import com.aaspaas.aaspaas_backend.delivery.dto.DeliveryPartnerMatchResponse;

import java.math.BigDecimal;
import java.util.List;

public interface DeliveryPartnerMatchingService {

    List<DeliveryPartnerMatchResponse> findMatches(
            BigDecimal pickupLatitude,
            BigDecimal pickupLongitude,
            BigDecimal destinationLatitude,
            BigDecimal destinationLongitude,
            BigDecimal searchRadiusKm
    );
}