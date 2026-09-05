package com.aaspaas.aaspaas_backend.auth.service;

import com.aaspaas.aaspaas_backend.auth.dto.LoginRequest;
import com.aaspaas.aaspaas_backend.auth.dto.LoginResponse;
import com.aaspaas.aaspaas_backend.auth.dto.RefreshTokenRequest;
import com.aaspaas.aaspaas_backend.auth.dto.RegisterRequest;
import com.aaspaas.aaspaas_backend.auth.dto.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    LoginResponse refreshAccessToken(
            RefreshTokenRequest request
    );

    void logout(RefreshTokenRequest request);
}