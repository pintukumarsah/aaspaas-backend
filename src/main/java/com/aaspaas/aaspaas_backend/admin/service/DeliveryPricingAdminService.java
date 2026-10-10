package com.aaspaas.aaspaas_backend.admin.service;

import com.aaspaas.aaspaas_backend.admin.dto.DeliveryPricingRuleRequest;
import com.aaspaas.aaspaas_backend.admin.dto.DeliveryPricingRuleResponse;
import org.springframework.data.domain.Page;

public interface DeliveryPricingAdminService {
    Page<DeliveryPricingRuleResponse> listRules(Boolean active, String deliveryMode, int page, int size);
    DeliveryPricingRuleResponse getRule(Long id);
    DeliveryPricingRuleResponse createRule(DeliveryPricingRuleRequest request);
    DeliveryPricingRuleResponse updateRule(Long id, DeliveryPricingRuleRequest request);
    DeliveryPricingRuleResponse setActive(Long id, boolean active);
}
