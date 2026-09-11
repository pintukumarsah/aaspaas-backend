package com.aaspaas.aaspaas_backend.delivery.serviceimpl;

import com.aaspaas.aaspaas_backend.delivery.dto.CreateDeliveryPartnerRequest;
import com.aaspaas.aaspaas_backend.delivery.dto.DeliveryPartnerResponse;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryPartner;
import com.aaspaas.aaspaas_backend.delivery.repository.DeliveryPartnerRepository;
import com.aaspaas.aaspaas_backend.user.entity.User;
import com.aaspaas.aaspaas_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.aaspaas.aaspaas_backend.delivery.service.DeliveryPartnerService;


@Service
@RequiredArgsConstructor
public class DeliveryPartnerServiceImpl
        implements DeliveryPartnerService {

    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final UserRepository userRepository;

    @Override
    public DeliveryPartnerResponse register(
            CreateDeliveryPartnerRequest request) {

        String phone = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByPhone(phone)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (deliveryPartnerRepository.existsByUserId(user.getId())) {
            throw new RuntimeException(
                    "User is already registered as delivery partner");
        }

        DeliveryPartner partner = DeliveryPartner.builder()
                .user(user)
                .vehicleType(request.getVehicleType())
                .vehicleNumber(request.getVehicleNumber())
                .verificationStatus("PENDING")
                .availabilityStatus("OFFLINE")
                .build();

        partner = deliveryPartnerRepository.save(partner);

        return mapToResponse(partner);
    }

    @Override
    public DeliveryPartnerResponse getMyProfile() {

        String phone = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        DeliveryPartner partner =
                deliveryPartnerRepository
                        .findByUserPhone(phone)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Delivery partner profile not found"));

        return mapToResponse(partner);
    }

    @Override
    public DeliveryPartnerResponse updateAvailability(String status) {

        String phone = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        DeliveryPartner partner =
                deliveryPartnerRepository
                        .findByUserPhone(phone)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Delivery partner profile not found"));

        if (!status.equals("ONLINE") &&
            !status.equals("OFFLINE") &&
            !status.equals("BUSY")) {

            throw new RuntimeException(
                    "Invalid availability status");
        }

        partner.setAvailabilityStatus(status);

        partner = deliveryPartnerRepository.save(partner);

        return mapToResponse(partner);
    }

    private DeliveryPartnerResponse mapToResponse(
            DeliveryPartner partner) {

        return DeliveryPartnerResponse.builder()
                .id(partner.getId())
                .userId(partner.getUser().getId())
                .vehicleType(partner.getVehicleType())
                .vehicleNumber(partner.getVehicleNumber())
                .verificationStatus(partner.getVerificationStatus())
                .availabilityStatus(partner.getAvailabilityStatus())
                .rating(partner.getRating())
                .totalDeliveries(partner.getTotalDeliveries())
                .build();
    }
}