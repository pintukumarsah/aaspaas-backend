package com.aaspaas.aaspaas_backend.delivery.service;

import com.aaspaas.aaspaas_backend.delivery.dto.CreateDeliveryPartnerRequest;
import com.aaspaas.aaspaas_backend.delivery.dto.DeliveryPartnerResponse;

public interface DeliveryPartnerService {

    DeliveryPartnerResponse register(CreateDeliveryPartnerRequest request);

    DeliveryPartnerResponse getMyProfile();

    DeliveryPartnerResponse updateAvailability(String status);
}