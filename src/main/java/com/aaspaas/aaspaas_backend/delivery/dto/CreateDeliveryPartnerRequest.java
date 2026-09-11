package com.aaspaas.aaspaas_backend.delivery.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDeliveryPartnerRequest {

    private String vehicleType;

    private String vehicleNumber;
}