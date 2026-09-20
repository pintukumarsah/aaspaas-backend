package com.aaspaas.aaspaas_backend.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
public class OtpResponse {

    private Long assignmentId;

    private String type;

    private OffsetDateTime expiresAt;

    private String developmentOtp;
}