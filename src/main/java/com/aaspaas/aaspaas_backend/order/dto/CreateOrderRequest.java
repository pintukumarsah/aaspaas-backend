package com.aaspaas.aaspaas_backend.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderRequest {

    @NotNull(message = "Delivery address ID is required")
    private Long deliveryAddressId;
}