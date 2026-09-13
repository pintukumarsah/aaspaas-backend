package com.aaspaas.aaspaas_backend.delivery.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateDeliveryStatusRequest {

    private String status;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String remarks;
}