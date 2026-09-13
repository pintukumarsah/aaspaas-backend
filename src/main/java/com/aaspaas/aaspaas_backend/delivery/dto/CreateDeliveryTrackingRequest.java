package com.aaspaas.aaspaas_backend.delivery.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateDeliveryTrackingRequest {

    private BigDecimal latitude;

    private BigDecimal longitude;

    private BigDecimal accuracyMeters;
}