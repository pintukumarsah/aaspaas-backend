package com.aaspaas.aaspaas_backend.auth.controller;

import com.aaspaas.aaspaas_backend.auth.dto.LoginRequest;
import com.aaspaas.aaspaas_backend.auth.dto.LoginResponse;
import com.aaspaas.aaspaas_backend.auth.dto.RefreshTokenRequest;
import com.aaspaas.aaspaas_backend.auth.dto.RegisterRequest;
import com.aaspaas.aaspaas_backend.auth.dto.RegisterResponse;
import com.aaspaas.aaspaas_backend.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        RegisterResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
public ResponseEntity<LoginResponse> login(
        @Valid @RequestBody LoginRequest request) {

    LoginResponse response = authService.login(request);

    return ResponseEntity.ok(response);
}

@PostMapping("/refresh")
public ResponseEntity<LoginResponse> refresh(
        @Valid @RequestBody RefreshTokenRequest request) {

    LoginResponse response =
            authService.refreshAccessToken(request);

    return ResponseEntity.ok(response);
}

@PostMapping("/logout")
public ResponseEntity<Void> logout(
        @Valid @RequestBody RefreshTokenRequest request) {

    authService.logout(request);

    return ResponseEntity.noContent().build();
}

}