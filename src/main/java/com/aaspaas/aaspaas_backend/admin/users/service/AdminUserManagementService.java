package com.aaspaas.aaspaas_backend.admin.users.service;

import com.aaspaas.aaspaas_backend.admin.users.dto.AdminUserSummary;
import org.springframework.data.domain.Page;

public interface AdminUserManagementService {
    Page<AdminUserSummary> searchUsers(String search, String status, String role, int page, int size);
    AdminUserSummary getUser(Long userId);
    AdminUserSummary suspendUser(Long userId, String adminPhone, String reason);
    AdminUserSummary reactivateUser(Long userId, String adminPhone, String reason);
}
