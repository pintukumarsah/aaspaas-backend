package com.aaspaas.aaspaas_backend.notification.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterDeviceRequest {

    private String deviceToken;

    private String platform;
}