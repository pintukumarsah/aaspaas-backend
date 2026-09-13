package com.aaspaas.aaspaas_backend.delivery.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class DeliveryOtpResponse {

    private Long assignmentId;

    private String otpType;

    private String otp;

    private OffsetDateTime expiresAt;
}