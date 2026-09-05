package com.aaspaas.aaspaas_backend.business.service;

import com.aaspaas.aaspaas_backend.business.dto.BusinessResponse;
import com.aaspaas.aaspaas_backend.business.dto.CreateBusinessRequest;

import java.util.List;

public interface BusinessService {

    BusinessResponse createBusiness(
            CreateBusinessRequest request,
            String phone
    );

    List<BusinessResponse> getMyBusinesses(String phone);

    BusinessResponse getBusinessById(Long businessId);
}