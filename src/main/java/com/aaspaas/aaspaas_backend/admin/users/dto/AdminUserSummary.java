package com.aaspaas.aaspaas_backend.admin.users.dto;

import com.aaspaas.aaspaas_backend.user.entity.Role;
import com.aaspaas.aaspaas_backend.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
public class AdminUserSummary {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String status;
    private boolean phoneVerified;
    private boolean emailVerified;
    private List<String> roles;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static AdminUserSummary fromEntity(User user) {
        return AdminUserSummary.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus())
                .phoneVerified(user.isPhoneVerified())
                .emailVerified(user.isEmailVerified())
                .roles(user.getRoles().stream().map(Role::getName).sorted().toList())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
