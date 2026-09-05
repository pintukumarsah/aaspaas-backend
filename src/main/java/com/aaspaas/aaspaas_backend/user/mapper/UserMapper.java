package com.aaspaas.aaspaas_backend.user.mapper;

import org.springframework.stereotype.Component;

import com.aaspaas.aaspaas_backend.user.dto.UserResponse;
import com.aaspaas.aaspaas_backend.user.entity.User;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {

        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setStatus(user.getStatus());
        response.setPhoneVerified(user.isPhoneVerified());
        response.setEmailVerified(user.isEmailVerified());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        return response;
    }
}