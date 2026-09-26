package com.aaspaas.aaspaas_backend.delivery.route.provider;

import com.aaspaas.aaspaas_backend.delivery.route.dto.RouteRequest;
import com.aaspaas.aaspaas_backend.delivery.route.dto.RouteResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class MockRoutingProvider implements RoutingProvider {

    @Override
    public RouteResponse calculateRoute(RouteRequest request) {

        double lat1 = request.getPickupLatitude().doubleValue();
        double lon1 = request.getPickupLongitude().doubleValue();

        double lat2 = request.getDeliveryLatitude().doubleValue();
        double lon2 = request.getDeliveryLongitude().doubleValue();

        double earthRadiusKm = 6371.0;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a =
                Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                        + Math.cos(Math.toRadians(lat1))
                        * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2)
                        * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double straightDistance = earthRadiusKm * c;

        /*
         * Mock road factor.
         *
         * Actual production value will come from
         * Google Maps / other routing provider.
         */
        double roadDistance = straightDistance * 1.25;

        int durationMinutes =
                (int) Math.ceil((roadDistance / 25.0) * 60);

        return RouteResponse.builder()
                .straightLineDistanceKm(
                        BigDecimal.valueOf(straightDistance)
                                .setScale(2, RoundingMode.HALF_UP)
                )
                .roadDistanceKm(
                        BigDecimal.valueOf(roadDistance)
                                .setScale(2, RoundingMode.HALF_UP)
                )
                .estimatedDurationMinutes(durationMinutes)
                .provider("MOCK")
                .status("SUCCESS")
                .build();
    }
}