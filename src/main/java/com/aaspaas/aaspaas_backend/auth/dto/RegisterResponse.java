package com.aaspaas.aaspaas_backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegisterResponse {

    private Long userId;

    private String fullName;

    private String phone;

    private String message;
}