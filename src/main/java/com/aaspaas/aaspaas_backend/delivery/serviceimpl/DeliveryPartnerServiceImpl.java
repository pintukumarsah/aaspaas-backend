package com.aaspaas.aaspaas_backend.delivery.serviceimpl;

import com.aaspaas.aaspaas_backend.delivery.dto.CreateDeliveryPartnerRequest;
import com.aaspaas.aaspaas_backend.delivery.dto.DeliveryPartnerResponse;
import com.aaspaas.aaspaas_backend.delivery.dto.PartnerRouteUpdateRequest;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryPartner;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryPartnerAvailabilityStatus;
import com.aaspaas.aaspaas_backend.delivery.repository.DeliveryPartnerRepository;
import com.aaspaas.aaspaas_backend.delivery.service.DeliveryPartnerService;
import com.aaspaas.aaspaas_backend.user.entity.User;
import com.aaspaas.aaspaas_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryPartnerServiceImpl
        implements DeliveryPartnerService {

    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final UserRepository userRepository;

    @Override
    public DeliveryPartnerResponse register(
            CreateDeliveryPartnerRequest request) {

        String phone = getAuthenticatedPhone();

        User user = userRepository.findByPhone(phone)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (deliveryPartnerRepository.existsByUserId(user.getId())) {
            throw new RuntimeException(
                    "User is already registered as delivery partner"
            );
        }

        DeliveryPartner partner = DeliveryPartner.builder()
                .user(user)
                .vehicleType(request.getVehicleType())
                .vehicleNumber(request.getVehicleNumber())
                .verificationStatus("PENDING")
                .availabilityStatus(DeliveryPartnerAvailabilityStatus.OFFLINE)
                .rating(BigDecimal.ZERO)
                .totalDeliveries(0)
                .routeAvailable(false)
                .build();

        partner = deliveryPartnerRepository.save(partner);

        return mapToResponse(partner);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryPartnerResponse getMyProfile() {

        String phone = getAuthenticatedPhone();

        DeliveryPartner partner =
                deliveryPartnerRepository
                        .findByUserPhone(phone)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Delivery partner profile not found"
                                )
                        );

        return mapToResponse(partner);
    }

    @Override
    public DeliveryPartnerResponse updateAvailability(
            String status) {

        String phone = getAuthenticatedPhone();

        DeliveryPartner partner =
                deliveryPartnerRepository
                        .findByUserPhone(phone)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Delivery partner profile not found"
                                )
                        );

        String normalizedStatus =
                status == null
                        ? ""
                        : status.trim().toUpperCase();

        DeliveryPartnerAvailabilityStatus newStatus;

        try {
            newStatus = DeliveryPartnerAvailabilityStatus.valueOf(normalizedStatus);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException(
                    "Invalid availability status. " +
                    "Allowed values: AVAILABLE, OFFLINE, BUSY"
            );
        }

        partner.setAvailabilityStatus(newStatus);

        /*
         * If partner goes OFFLINE, their route should not
         * remain publicly matchable.
         */
        if (newStatus == DeliveryPartnerAvailabilityStatus.OFFLINE) {
            partner.setRouteAvailable(false);
        }

        partner = deliveryPartnerRepository.save(partner);

        return mapToResponse(partner);
    }

    @Override
    public void updateRoute(
            PartnerRouteUpdateRequest request) {

        String phone = getAuthenticatedPhone();

        DeliveryPartner partner =
                deliveryPartnerRepository
                        .findByUserPhone(phone)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Delivery partner profile not found"
                                )
                        );

        if (partner.getAvailabilityStatus() != DeliveryPartnerAvailabilityStatus.AVAILABLE) {
            throw new RuntimeException(
                    "Delivery partner must be AVAILABLE to publish a route"
            );
        }

        if (request.currentLatitude()
                .compareTo(BigDecimal.valueOf(90)) > 0
                || request.currentLatitude()
                .compareTo(BigDecimal.valueOf(-90)) < 0) {

            throw new RuntimeException(
                    "Invalid current latitude"
            );
        }

        if (request.currentLongitude()
                .compareTo(BigDecimal.valueOf(180)) > 0
                || request.currentLongitude()
                .compareTo(BigDecimal.valueOf(-180)) < 0) {

            throw new RuntimeException(
                    "Invalid current longitude"
            );
        }

        if (request.destinationLatitude()
                .compareTo(BigDecimal.valueOf(90)) > 0
                || request.destinationLatitude()
                .compareTo(BigDecimal.valueOf(-90)) < 0) {

            throw new RuntimeException(
                    "Invalid destination latitude"
            );
        }

        if (request.destinationLongitude()
                .compareTo(BigDecimal.valueOf(180)) > 0
                || request.destinationLongitude()
                .compareTo(BigDecimal.valueOf(-180)) < 0) {

            throw new RuntimeException(
                    "Invalid destination longitude"
            );
        }

        if (request.plannedDepartureAt() != null
                && request.plannedDepartureAt()
                .isBefore(OffsetDateTime.now())) {

            throw new RuntimeException(
                    "Planned departure time cannot be in the past"
            );
        }

        boolean routeAvailable =
                request.routeAvailable() == null
                        || request.routeAvailable();

        partner.setCurrentLatitude(
                request.currentLatitude()
        );

        partner.setCurrentLongitude(
                request.currentLongitude()
        );

        partner.setDestinationLatitude(
                request.destinationLatitude()
        );

        partner.setDestinationLongitude(
                request.destinationLongitude()
        );

        partner.setDestinationName(
                request.destinationName().trim()
        );

        partner.setPlannedDepartureAt(
                request.plannedDepartureAt()
        );

        partner.setRouteAvailable(routeAvailable);

        partner.setLocationUpdatedAt(
                OffsetDateTime.now()
        );

        deliveryPartnerRepository.save(partner);
    }

    private String getAuthenticatedPhone() {

        if (SecurityContextHolder
                .getContext()
                .getAuthentication() == null) {

            throw new RuntimeException(
                    "Authentication required"
            );
        }

        return SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
    }

    private DeliveryPartnerResponse mapToResponse(
            DeliveryPartner partner) {

        return DeliveryPartnerResponse.builder()
                .id(partner.getId())
                .userId(partner.getUser().getId())
                .vehicleType(partner.getVehicleType())
                .vehicleNumber(partner.getVehicleNumber())
                .verificationStatus(
                        partner.getVerificationStatus()
                )
                .availabilityStatus(
                        partner.getAvailabilityStatus().name()
                )
                .rating(partner.getRating())
                .totalDeliveries(
                        partner.getTotalDeliveries()
                )
                .build();
    }
}