package com.aaspaas.aaspaas_backend.admin.service;

import com.aaspaas.aaspaas_backend.delivery.dto.DeliveryPartnerResponse;
import org.springframework.data.domain.Page;

public interface DeliveryPartnerModerationService {
    Page<DeliveryPartnerResponse> getPartners(String status, int page, int size);
    DeliveryPartnerResponse approve(Long partnerId);
    DeliveryPartnerResponse reject(Long partnerId);
    DeliveryPartnerResponse suspend(Long partnerId);
    DeliveryPartnerResponse reactivate(Long partnerId);
}
