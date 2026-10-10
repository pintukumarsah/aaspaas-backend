package com.aaspaas.aaspaas_backend.admin.serviceimpl;

import com.aaspaas.aaspaas_backend.admin.service.DeliveryPartnerModerationService;
import com.aaspaas.aaspaas_backend.common.exception.BusinessException;
import com.aaspaas.aaspaas_backend.delivery.dto.DeliveryPartnerResponse;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryPartner;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryPartnerAvailabilityStatus;
import com.aaspaas.aaspaas_backend.delivery.repository.DeliveryPartnerRepository;
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
public class DeliveryPartnerModerationServiceImpl implements DeliveryPartnerModerationService {

    private static final Set<String> ALLOWED_STATUSES =
            Set.of("PENDING", "APPROVED", "REJECTED", "SUSPENDED");

    private final DeliveryPartnerRepository deliveryPartnerRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<DeliveryPartnerResponse> getPartners(String status, int page, int size) {
        validatePage(page, size);
        PageRequest pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<DeliveryPartner> result;
        if (status == null || status.isBlank()) {
            result = deliveryPartnerRepository.findAll(pageable);
        } else {
            String normalized = normalizeStatus(status);
            result = deliveryPartnerRepository.findByVerificationStatus(normalized, pageable);
        }
        return result.map(this::toResponse);
    }

    @Override
    @Transactional
    public DeliveryPartnerResponse approve(Long partnerId) {
        DeliveryPartner partner = getForUpdate(partnerId);
        requireStatus(partner, "PENDING", "Only a pending partner can be approved");
        partner.setVerificationStatus("APPROVED");
        return toResponse(deliveryPartnerRepository.save(partner));
    }

    @Override
    @Transactional
    public DeliveryPartnerResponse reject(Long partnerId) {
        DeliveryPartner partner = getForUpdate(partnerId);
        requireStatus(partner, "PENDING", "Only a pending partner can be rejected");
        partner.setVerificationStatus("REJECTED");
        partner.setAvailabilityStatus(DeliveryPartnerAvailabilityStatus.OFFLINE);
        partner.setRouteAvailable(false);
        return toResponse(deliveryPartnerRepository.save(partner));
    }

    @Override
    @Transactional
    public DeliveryPartnerResponse suspend(Long partnerId) {
        DeliveryPartner partner = getForUpdate(partnerId);
        requireStatus(partner, "APPROVED", "Only an approved partner can be suspended");
        partner.setVerificationStatus("SUSPENDED");
        partner.setAvailabilityStatus(DeliveryPartnerAvailabilityStatus.OFFLINE);
        partner.setRouteAvailable(false);
        return toResponse(deliveryPartnerRepository.save(partner));
    }

    @Override
    @Transactional
    public DeliveryPartnerResponse reactivate(Long partnerId) {
        DeliveryPartner partner = getForUpdate(partnerId);
        requireStatus(partner, "SUSPENDED", "Only a suspended partner can be reactivated");
        partner.setVerificationStatus("APPROVED");
        partner.setAvailabilityStatus(DeliveryPartnerAvailabilityStatus.OFFLINE);
        partner.setRouteAvailable(false);
        return toResponse(deliveryPartnerRepository.save(partner));
    }

    private DeliveryPartner getForUpdate(Long partnerId) {
        if (partnerId == null || partnerId <= 0) {
            throw new BusinessException("A valid delivery partner ID is required", 400);
        }
        return deliveryPartnerRepository.findByIdForUpdate(partnerId)
                .orElseThrow(() -> new BusinessException("Delivery partner not found", 404));
    }

    private void requireStatus(DeliveryPartner partner, String expected, String message) {
        if (!expected.equals(partner.getVerificationStatus())) {
            throw new BusinessException(message, 409);
        }
    }

    private String normalizeStatus(String status) {
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new BusinessException("Unsupported verification status. Allowed values: "
                    + String.join(", ", ALLOWED_STATUSES), 400);
        }
        return normalized;
    }

    private void validatePage(int page, int size) {
        if (page < 0) throw new BusinessException("Page cannot be negative", 400);
        if (size < 1 || size > 100) {
            throw new BusinessException("Page size must be between 1 and 100", 400);
        }
    }

    private DeliveryPartnerResponse toResponse(DeliveryPartner partner) {
        return DeliveryPartnerResponse.builder()
                .id(partner.getId())
                .userId(partner.getUser().getId())
                .vehicleType(partner.getVehicleType())
                .vehicleNumber(partner.getVehicleNumber())
                .verificationStatus(partner.getVerificationStatus())
                .availabilityStatus(partner.getAvailabilityStatus().name())
                .rating(partner.getRating())
                .totalDeliveries(partner.getTotalDeliveries())
                .build();
    }
}
