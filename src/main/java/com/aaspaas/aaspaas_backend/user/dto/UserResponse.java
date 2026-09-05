package com.aaspaas.aaspaas_backend.user.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class UserResponse {

    private Long id;

    private String fullName;

    private String email;

    private String phone;

    private String status;

    private boolean phoneVerified;

    private boolean emailVerified;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}