package com.aaspaas.aaspaas_backend.business.controller;

import com.aaspaas.aaspaas_backend.business.dto.BusinessResponse;
import com.aaspaas.aaspaas_backend.business.dto.CreateBusinessRequest;
import com.aaspaas.aaspaas_backend.business.service.BusinessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/businesses")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessService businessService;

    @PostMapping
    public ResponseEntity<BusinessResponse> createBusiness(
            @Valid @RequestBody CreateBusinessRequest request,
            Authentication authentication
    ) {

        BusinessResponse response =
                businessService.createBusiness(
                        request,
                        authentication.getName()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<BusinessResponse>> getMyBusinesses(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                businessService.getMyBusinesses(
                        authentication.getName()
                )
        );
    }

    @GetMapping("/{businessId}")
    public ResponseEntity<BusinessResponse> getBusinessById(
            @PathVariable Long businessId
    ) {

        return ResponseEntity.ok(
                businessService.getBusinessById(businessId)
        );
    }
}