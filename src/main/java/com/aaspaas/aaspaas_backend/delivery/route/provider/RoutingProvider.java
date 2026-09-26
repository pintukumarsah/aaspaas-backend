package com.aaspaas.aaspaas_backend.delivery.route.provider;

import com.aaspaas.aaspaas_backend.delivery.route.dto.RouteRequest;
import com.aaspaas.aaspaas_backend.delivery.route.dto.RouteResponse;

public interface RoutingProvider {

    RouteResponse calculateRoute(RouteRequest request);
}