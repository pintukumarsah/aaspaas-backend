package com.aaspaas.aaspaas_backend.business.dto;

import java.math.BigDecimal;

import com.aaspaas.aaspaas_backend.business.entity.Business;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BusinessResponse {

    private Long id;

    private Long ownerId;

    private String name;

    private String description;

    private String businessType;

    private String phone;

    private Long addressId;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String status;

    public static BusinessResponse fromEntity(Business business) {

        return new BusinessResponse(
                business.getId(),
                business.getOwner().getId(),
                business.getName(),
                business.getDescription(),
                business.getBusinessType(),
                business.getPhone(),
                business.getAddressId(),
                business.getLatitude(),
                business.getLongitude(),
                business.getStatus()
        );
    }
}