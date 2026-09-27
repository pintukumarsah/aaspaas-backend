package com.aaspaas.aaspaas_backend.business.dto;

import com.aaspaas.aaspaas_backend.business.entity.Business;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

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

        if (business == null) {
            return null;
        }

        return new BusinessResponse(
                business.getId(),
                business.getOwner() != null
                        ? business.getOwner().getId()
                        : null,
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