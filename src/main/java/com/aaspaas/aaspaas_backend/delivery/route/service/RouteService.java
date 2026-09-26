package com.aaspaas.aaspaas_backend.delivery.route.service;

import com.aaspaas.aaspaas_backend.delivery.route.dto.RouteRequest;
import com.aaspaas.aaspaas_backend.delivery.route.dto.RouteResponse;

public interface RouteService {

    RouteResponse calculateRoute(RouteRequest request);
}