package com.aaspaas.aaspaas_backend.auth.serviceimpl;

import com.aaspaas.aaspaas_backend.auth.dto.RegisterRequest;
import com.aaspaas.aaspaas_backend.auth.dto.RegisterResponse;
import com.aaspaas.aaspaas_backend.auth.service.AuthService;
import com.aaspaas.aaspaas_backend.auth.service.RefreshTokenService;
import com.aaspaas.aaspaas_backend.common.exception.BusinessException;
import com.aaspaas.aaspaas_backend.user.entity.User;
import com.aaspaas.aaspaas_backend.user.repository.RoleRepository;
import com.aaspaas.aaspaas_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.aaspaas.aaspaas_backend.auth.dto.LoginRequest;
import com.aaspaas.aaspaas_backend.auth.dto.LoginResponse;
import com.aaspaas.aaspaas_backend.auth.dto.RefreshTokenRequest;
import com.aaspaas.aaspaas_backend.security.JwtService;
import com.aaspaas.aaspaas_backend.user.entity.Role;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final RefreshTokenService refreshTokenService;
        private final RoleRepository roleRepository;

        @Override
        public RegisterResponse register(RegisterRequest request) {

                if (userRepository.existsByPhone(request.getPhone())) {
                        throw new BusinessException(
                                        "Phone number already registered",
                                        409);
                }

                if (request.getEmail() != null
                                && !request.getEmail().isBlank()
                                && userRepository.existsByEmail(request.getEmail())) {

                        throw new BusinessException(
                                        "Email already registered",
                                        409);
                }

                User user = new User();

                user.setFullName(request.getFullName());
                user.setEmail(request.getEmail());
                user.setPhone(request.getPhone());

                user.setPasswordHash(
                                passwordEncoder.encode(request.getPassword()));

                user.setStatus("ACTIVE");
                user.setPhoneVerified(false);
                user.setEmailVerified(false);
                Role customerRole = roleRepository
                                .findByName("CUSTOMER")
                                .orElseThrow(() -> new BusinessException(
                                                "CUSTOMER role not found",
                                                500));

                user.getRoles().add(customerRole);
                User savedUser = userRepository.save(user);

                return new RegisterResponse(
                                savedUser.getId(),
                                savedUser.getFullName(),
                                savedUser.getPhone(),
                                "User registered successfully");
        }

        @Override
        public LoginResponse login(LoginRequest request) {

                User user = userRepository.findByPhone(request.getPhone())
                                .orElseThrow(() -> new BusinessException(
                                                "Invalid phone number or password",
                                                401));

                if (!passwordEncoder.matches(
                                request.getPassword(),
                                user.getPasswordHash())) {

                        throw new BusinessException(
                                        "Invalid phone number or password",
                                        401);
                }

                if (!"ACTIVE".equals(user.getStatus())) {

                        throw new BusinessException(
                                        "User account is not active",
                                        403);
                }

                String accessToken = jwtService.generateAccessToken(
                                user.getId(),
                                user.getPhone());

                String refreshToken = refreshTokenService.createRefreshToken(user);

                return new LoginResponse(
                                accessToken,
                                refreshToken,
                                "Bearer",
                                900,
                                user.getId(),
                                user.getFullName());
        }

        @Override
        public LoginResponse refreshAccessToken(
                        RefreshTokenRequest request) {

                var refreshToken = refreshTokenService.verifyRefreshToken(
                                request.getRefreshToken());

                User user = refreshToken.getUser();

                String accessToken = jwtService.generateAccessToken(
                                user.getId(),
                                user.getPhone());

                return new LoginResponse(
                                accessToken,
                                request.getRefreshToken(),
                                "Bearer",
                                900,
                                user.getId(),
                                user.getFullName());
        }

        @Override
        public void logout(RefreshTokenRequest request) {

                refreshTokenService.revokeToken(
                                request.getRefreshToken());
        }
}
