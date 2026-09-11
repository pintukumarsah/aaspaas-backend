package com.aaspaas.aaspaas_backend.delivery.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DeliveryPartnerResponse {

    private Long id;

    private Long userId;

    private String vehicleType;

    private String vehicleNumber;

    private String verificationStatus;

    private String availabilityStatus;

    private BigDecimal rating;

    private Integer totalDeliveries;
}