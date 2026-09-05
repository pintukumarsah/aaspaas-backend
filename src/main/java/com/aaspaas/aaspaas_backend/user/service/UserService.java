package com.aaspaas.aaspaas_backend.user.service;

import java.util.List;

import com.aaspaas.aaspaas_backend.user.dto.UserResponse;

public interface UserService {

    UserResponse getUserById(Long id);

    List<UserResponse> getAllUsers();
}