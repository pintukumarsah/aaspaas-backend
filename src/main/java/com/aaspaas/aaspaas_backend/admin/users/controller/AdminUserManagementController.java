package com.aaspaas.aaspaas_backend.admin.users.controller;

import com.aaspaas.aaspaas_backend.admin.users.dto.AdminUserSummary;
import com.aaspaas.aaspaas_backend.admin.users.service.AdminUserManagementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserManagementController {
    private final AdminUserManagementService service;

    @GetMapping
    public ResponseEntity<Page<AdminUserSummary>> searchUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.searchUsers(search, status, role, page, size));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserSummary> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getUser(userId));
    }

    @PatchMapping("/{userId}/suspend")
    public ResponseEntity<AdminUserSummary> suspend(
            @PathVariable Long userId,
            @Valid @RequestBody AccountActionRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(service.suspendUser(userId, authentication.getName(), request.getReason()));
    }

    @PatchMapping("/{userId}/reactivate")
    public ResponseEntity<AdminUserSummary> reactivate(
            @PathVariable Long userId,
            @Valid @RequestBody AccountActionRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(service.reactivateUser(userId, authentication.getName(), request.getReason()));
    }

    @Getter
    @Setter
    public static class AccountActionRequest {
        @NotBlank
        @Size(min = 5, max = 500)
        private String reason;
    }
}
