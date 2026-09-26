package com.aaspaas.aaspaas_backend.delivery.route.serviceimpl;
import com.aaspaas.aaspaas_backend.delivery.route.service.RouteService;
import com.aaspaas.aaspaas_backend.delivery.route.dto.RouteRequest;
import com.aaspaas.aaspaas_backend.delivery.route.dto.RouteResponse;
import com.aaspaas.aaspaas_backend.delivery.route.provider.RoutingProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

    private final RoutingProvider routingProvider;

    @Override
    public RouteResponse calculateRoute(RouteRequest request) {

        validateCoordinates(request);

        return routingProvider.calculateRoute(request);
    }

    private void validateCoordinates(RouteRequest request) {

        if (request.getPickupLatitude() == null
                || request.getPickupLongitude() == null
                || request.getDeliveryLatitude() == null
                || request.getDeliveryLongitude() == null) {

            throw new IllegalArgumentException(
                    "Pickup and delivery coordinates are required"
            );
        }
    }
}
