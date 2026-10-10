package com.aaspaas.aaspaas_backend.admin.users.serviceimpl;

import com.aaspaas.aaspaas_backend.admin.users.dto.AdminUserSummary;
import com.aaspaas.aaspaas_backend.admin.users.service.AdminUserManagementService;
import com.aaspaas.aaspaas_backend.user.entity.User;
import com.aaspaas.aaspaas_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminUserManagementServiceImpl implements AdminUserManagementService {
    private static final Set<String> ALLOWED_STATUSES = Set.of("ACTIVE", "SUSPENDED", "INACTIVE");
    private static final Set<String> ALLOWED_ROLES =
            Set.of("ADMIN", "CUSTOMER", "SELLER", "DELIVERY_PARTNER", "SERVICE_PROVIDER");

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserSummary> searchUsers(String search, String status, String role, int page, int size) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page cannot be negative");
        }
        if (size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page size must be between 1 and 100");
        }

        String normalizedStatus = normalizeOptional(status);
        String normalizedRole = normalizeOptional(role);
        String normalizedSearch = search == null || search.isBlank() ? null : search.trim();

        if (normalizedStatus != null && !ALLOWED_STATUSES.contains(normalizedStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported user status");
        }
        if (normalizedRole != null && !ALLOWED_ROLES.contains(normalizedRole)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported role");
        }
        if (normalizedSearch != null && normalizedSearch.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search text cannot exceed 100 characters");
        }

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return userRepository.searchForAdmin(normalizedSearch, normalizedStatus, normalizedRole, pageable)
                .map(AdminUserSummary::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserSummary getUser(Long userId) {
        return AdminUserSummary.fromEntity(findUser(userId));
    }

    @Override
    @Transactional
    public AdminUserSummary suspendUser(Long userId, String adminPhone, String reason) {
        User target = findUser(userId);
        validateReason(reason);

        if (target.getPhone().equals(adminPhone)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An administrator cannot suspend their own account");
        }
        if ("SUSPENDED".equalsIgnoreCase(target.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already suspended");
        }

        boolean targetIsAdmin = target.getRoles().stream()
                .anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getName()));
        if (targetIsAdmin && "ACTIVE".equalsIgnoreCase(target.getStatus())) {
            long activeAdmins = userRepository.countActiveAdmins();
            if (activeAdmins <= 1) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "The last active administrator cannot be suspended");
            }
        }

        target.setStatus("SUSPENDED");
        return AdminUserSummary.fromEntity(userRepository.save(target));
    }

    @Override
    @Transactional
    public AdminUserSummary reactivateUser(Long userId, String adminPhone, String reason) {
        User target = findUser(userId);
        validateReason(reason);

        if ("ACTIVE".equalsIgnoreCase(target.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already active");
        }
        target.setStatus("ACTIVE");
        return AdminUserSummary.fromEntity(userRepository.save(target));
    }

    private User findUser(Long userId) {
        if (userId == null || userId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid user ID is required");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private void validateReason(String reason) {
        if (reason == null || reason.isBlank() || reason.trim().length() < 5 || reason.trim().length() > 500) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reason must contain 5 to 500 characters");
        }
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase(Locale.ROOT);
    }
}
