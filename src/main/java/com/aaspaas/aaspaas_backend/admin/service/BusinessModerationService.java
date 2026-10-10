package com.aaspaas.aaspaas_backend.admin.service;

import com.aaspaas.aaspaas_backend.business.dto.BusinessResponse;
import org.springframework.data.domain.Page;

public interface BusinessModerationService {

    Page<BusinessResponse> getBusinesses(
            String status,
            int page,
            int size
    );

    BusinessResponse approveBusiness(Long businessId);

    BusinessResponse rejectBusiness(Long businessId);

    BusinessResponse suspendBusiness(Long businessId);

    BusinessResponse reactivateBusiness(Long businessId);
}