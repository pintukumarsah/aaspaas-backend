package com.aaspaas.aaspaas_backend.delivery.serviceimpl;

import com.aaspaas.aaspaas_backend.common.util.DistanceUtils;
import com.aaspaas.aaspaas_backend.delivery.dto.DeliveryPartnerMatchResponse;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryPartner;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryPartnerAvailabilityStatus;
import com.aaspaas.aaspaas_backend.delivery.repository.DeliveryPartnerRepository;
import com.aaspaas.aaspaas_backend.delivery.service.DeliveryPartnerMatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryPartnerMatchingServiceImpl
        implements DeliveryPartnerMatchingService {

    private final DeliveryPartnerRepository partnerRepository;

    @Override
    public List<DeliveryPartnerMatchResponse> findMatches(
            BigDecimal pickupLatitude,
            BigDecimal pickupLongitude,
            BigDecimal destinationLatitude,
            BigDecimal destinationLongitude,
            BigDecimal searchRadiusKm) {

        validateInput(
                pickupLatitude,
                pickupLongitude,
                destinationLatitude,
                destinationLongitude,
                searchRadiusKm
        );

        /*
         * Consider location fresh only for the last 10 minutes.
         *
         * Later this can become configurable.
         */
        OffsetDateTime minimumLocationTime =
                OffsetDateTime.now().minusMinutes(10);

        List<DeliveryPartner> partners =
                partnerRepository.findEligibleRoutePartners(
                        minimumLocationTime,
                        DeliveryPartnerAvailabilityStatus.AVAILABLE
                );

        return partners.stream()
                .map(partner ->
                        createMatch(
                                partner,
                                pickupLatitude,
                                pickupLongitude,
                                destinationLatitude,
                                destinationLongitude,
                                searchRadiusKm
                        )
                )
                .filter(match -> match != null)
                .sorted(
                        Comparator.comparing(
                                DeliveryPartnerMatchResponse
                                        ::getDistanceFromPickupKm
                        )
                )
                .toList();
    }

    private DeliveryPartnerMatchResponse createMatch(
            DeliveryPartner partner,
            BigDecimal pickupLatitude,
            BigDecimal pickupLongitude,
            BigDecimal destinationLatitude,
            BigDecimal destinationLongitude,
            BigDecimal searchRadiusKm) {

        BigDecimal pickupDistance =
                DistanceUtils.distanceInKm(
                        partner.getCurrentLatitude(),
                        partner.getCurrentLongitude(),
                        pickupLatitude,
                        pickupLongitude
                );

        BigDecimal destinationDistance =
                DistanceUtils.distanceInKm(
                        partner.getDestinationLatitude(),
                        partner.getDestinationLongitude(),
                        destinationLatitude,
                        destinationLongitude
                );

        String matchType =
                determineMatchType(
                        pickupDistance,
                        destinationDistance,
                        searchRadiusKm
                );

        if (matchType == null) {
            return null;
        }

        Integer estimatedMinutes =
                calculateEstimatedMinutes(
                        pickupDistance,
                        partner.getPlannedDepartureAt()
                );

        return DeliveryPartnerMatchResponse.builder()
                .deliveryPartnerId(partner.getId())
                .userId(partner.getUser().getId())
                .partnerName(
                        partner.getUser().getFullName()
                )
                .distanceFromPickupKm(
                        pickupDistance
                )
                .distanceFromDestinationKm(
                        destinationDistance
                )
                .destinationName(
                        partner.getDestinationName()
                )
                .plannedDepartureAt(
                        partner.getPlannedDepartureAt()
                )
                .estimatedMinutes(
                        estimatedMinutes
                )
                .matchType(matchType)
                .routeAvailable(
                        partner.getRouteAvailable()
                )
                .build();
    }

    private String determineMatchType(
            BigDecimal pickupDistance,
            BigDecimal destinationDistance,
            BigDecimal searchRadiusKm) {

        boolean nearPickup =
                pickupDistance.compareTo(searchRadiusKm) <= 0;

        boolean nearDestination =
                destinationDistance.compareTo(searchRadiusKm) <= 0;

        /*
         * Strongest match:
         *
         * Partner is near pickup
         * AND
         * partner's planned destination is near customer's destination.
         */
        if (nearPickup && nearDestination) {
            return "ROUTE_MATCH";
        }

        /*
         * Partner is near pickup but destination differs.
         *
         * This can later be offered as DIRECT_NEARBY
         * if business rules allow it.
         */
        if (nearPickup) {
            return "DIRECT_NEARBY";
        }

        /*
         * Partner is close to destination but far from pickup.
         *
         * We keep this as a weaker candidate.
         */
        if (nearDestination) {
            return "NEARBY_DESTINATION";
        }

        return null;
    }

    private Integer calculateEstimatedMinutes(
            BigDecimal pickupDistance,
            OffsetDateTime plannedDepartureAt) {

        /*
         * If partner has a planned departure time,
         * return the number of minutes until departure.
         */
        if (plannedDepartureAt != null) {

            long minutes =
                    Duration.between(
                            OffsetDateTime.now(),
                            plannedDepartureAt
                    ).toMinutes();

            return Math.max(
                    0,
                    (int) Math.min(
                            minutes,
                            Integer.MAX_VALUE
                    )
            );
        }

        /*
         * Rough MVP estimate.
         *
         * 15 km/h average local movement assumption.
         *
         * This is NOT road ETA.
         * Google Routes/Maps can replace this later.
         */
        double hours =
                pickupDistance.doubleValue() / 15.0;

        return Math.max(
                1,
                (int) Math.ceil(hours * 60)
        );
    }

    private void validateInput(
            BigDecimal pickupLatitude,
            BigDecimal pickupLongitude,
            BigDecimal destinationLatitude,
            BigDecimal destinationLongitude,
            BigDecimal searchRadiusKm) {

        if (pickupLatitude == null
                || pickupLongitude == null
                || destinationLatitude == null
                || destinationLongitude == null) {

            throw new IllegalArgumentException(
                    "Pickup and destination coordinates are required"
            );
        }

        if (searchRadiusKm == null
                || searchRadiusKm.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Search radius must be greater than zero"
            );
        }

        if (pickupLatitude.compareTo(
                BigDecimal.valueOf(-90)) < 0
                || pickupLatitude.compareTo(
                BigDecimal.valueOf(90)) > 0) {

            throw new IllegalArgumentException(
                    "Invalid pickup latitude"
            );
        }

        if (destinationLatitude.compareTo(
                BigDecimal.valueOf(-90)) < 0
                || destinationLatitude.compareTo(
                BigDecimal.valueOf(90)) > 0) {

            throw new IllegalArgumentException(
                    "Invalid destination latitude"
            );
        }

        if (pickupLongitude.compareTo(
                BigDecimal.valueOf(-180)) < 0
                || pickupLongitude.compareTo(
                BigDecimal.valueOf(180)) > 0) {

            throw new IllegalArgumentException(
                    "Invalid pickup longitude"
            );
        }

        if (destinationLongitude.compareTo(
                BigDecimal.valueOf(-180)) < 0
                || destinationLongitude.compareTo(
                BigDecimal.valueOf(180)) > 0) {

            throw new IllegalArgumentException(
                    "Invalid destination longitude"
            );
        }
    }
}