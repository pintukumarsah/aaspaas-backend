package com.aaspaas.aaspaas_backend.delivery.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpRequest {

    @NotBlank(message = "OTP is required")
    private String otp;
}