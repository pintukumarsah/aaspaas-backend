package com.aaspaas.aaspaas_backend.admin.serviceimpl;

import com.aaspaas.aaspaas_backend.admin.service.BusinessModerationService;
import com.aaspaas.aaspaas_backend.business.dto.BusinessResponse;
import com.aaspaas.aaspaas_backend.business.entity.Business;
import com.aaspaas.aaspaas_backend.business.repository.BusinessRepository;
import com.aaspaas.aaspaas_backend.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BusinessModerationServiceImpl
        implements BusinessModerationService {

    private static final Set<String> ALLOWED_STATUSES =
            Set.of("ACTIVE", "REJECTED", "SUSPENDED");

    private final BusinessRepository businessRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<BusinessResponse> getBusinesses(
            String status,
            int page,
            int size
    ) {
        if (page < 0) {
            throw new BusinessException(
                    "Page cannot be negative", 400
            );
        }

        if (size < 1 || size > 100) {
            throw new BusinessException(
                    "Page size must be between 1 and 100", 400
            );
        }

        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Business> businesses;

        if (status == null || status.isBlank()) {
            businesses = businessRepository.findAll(pageable);
        } else {
            String normalizedStatus =
                    status.trim().toUpperCase(Locale.ROOT);

            if (!ALLOWED_STATUSES.contains(normalizedStatus)) {
                throw new BusinessException(
                        "Unsupported business status", 400
                );
            }

            businesses = businessRepository
                    .findByStatus(normalizedStatus, pageable);
        }

        return businesses.map(BusinessResponse::fromEntity);
    }

    @Override
    @Transactional
    public BusinessResponse approveBusiness(Long businessId) {
        Business business = getBusinessOrThrow(businessId);

        if ("SUSPENDED".equals(business.getStatus())) {
            throw new BusinessException(
                    "Suspended business must be reviewed before approval",
                    409
            );
        }

        business.setStatus("ACTIVE");

        return BusinessResponse.fromEntity(
                businessRepository.save(business)
        );
    }

    @Override
    @Transactional
    public BusinessResponse rejectBusiness(Long businessId) {
        Business business = getBusinessOrThrow(businessId);

        if ("SUSPENDED".equals(business.getStatus())) {
            throw new BusinessException(
                    "Suspended business cannot be rejected directly",
                    409
            );
        }

        business.setStatus("REJECTED");

        return BusinessResponse.fromEntity(
                businessRepository.save(business)
        );
    }

    @Override
    @Transactional
    public BusinessResponse suspendBusiness(Long businessId) {
        Business business = getBusinessOrThrow(businessId);

        if (!"ACTIVE".equals(business.getStatus())) {
            throw new BusinessException(
                    "Only an active business can be suspended",
                    409
            );
        }

        business.setStatus("SUSPENDED");

        return BusinessResponse.fromEntity(
                businessRepository.save(business)
        );
    }

    @Override
    @Transactional
    public BusinessResponse reactivateBusiness(Long businessId) {
        Business business = getBusinessOrThrow(businessId);

        if (!"SUSPENDED".equals(business.getStatus())) {
            throw new BusinessException(
                    "Only a suspended business can be reactivated",
                    409
            );
        }

        business.setStatus("ACTIVE");

        return BusinessResponse.fromEntity(
                businessRepository.save(business)
        );
    }

    private Business getBusinessOrThrow(Long businessId) {
        if (businessId == null || businessId <= 0) {
            throw new BusinessException(
                    "A valid business ID is required", 400
            );
        }

        return businessRepository.findById(businessId)
                .orElseThrow(() ->
                        new BusinessException(
                                "Business not found", 404
                        )
                );
    }
}