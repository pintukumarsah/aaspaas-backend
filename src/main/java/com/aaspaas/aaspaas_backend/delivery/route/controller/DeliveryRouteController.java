package com.aaspaas.aaspaas_backend.delivery.route.controller;

import com.aaspaas.aaspaas_backend.delivery.route.dto.RouteRequest;
import com.aaspaas.aaspaas_backend.delivery.route.dto.RouteResponse;
import com.aaspaas.aaspaas_backend.delivery.route.service.RouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/delivery/routes")
@RequiredArgsConstructor
public class DeliveryRouteController {

    private final RouteService routeService;

    @PostMapping("/calculate")
    public ResponseEntity<RouteResponse> calculateRoute(
            @Valid @RequestBody RouteRequest request) {

        return ResponseEntity.ok(
                routeService.calculateRoute(request)
        );
    }
}