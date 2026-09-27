package com.aaspaas.aaspaas_backend.auth.serviceimpl;

import com.aaspaas.aaspaas_backend.auth.entity.RefreshToken;
import com.aaspaas.aaspaas_backend.auth.repository.RefreshTokenRepository;
import com.aaspaas.aaspaas_backend.auth.service.RefreshTokenService;
import com.aaspaas.aaspaas_backend.common.exception.BusinessException;
import com.aaspaas.aaspaas_backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl
        implements RefreshTokenService {

    private static final int TOKEN_BYTES = 32;

    private static final int TOKEN_EXPIRY_DAYS = 7;

    private final RefreshTokenRepository refreshTokenRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public String createRefreshToken(User user) {

        if (user == null || user.getId() == null) {
            throw new BusinessException(
                    "Valid user is required to create refresh token",
                    400
            );
        }

        byte[] randomBytes =
                new byte[TOKEN_BYTES];

        secureRandom.nextBytes(randomBytes);

        String rawToken =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(randomBytes);

        RefreshToken refreshToken =
                new RefreshToken();

        refreshToken.setUser(user);

        refreshToken.setTokenHash(
                hashToken(rawToken)
        );

        refreshToken.setExpiresAt(
                OffsetDateTime.now()
                        .plusDays(TOKEN_EXPIRY_DAYS)
        );

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken verifyRefreshToken(
            String rawToken
    ) {

        if (rawToken == null ||
                rawToken.isBlank()) {

            throw new BusinessException(
                    "Refresh token is required",
                    401
            );
        }

        String tokenHash =
                hashToken(rawToken);

        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Invalid refresh token",
                                        401
                                )
                        );

        if (refreshToken.getRevokedAt() != null) {

            throw new BusinessException(
                    "Refresh token has been revoked",
                    401
            );
        }

        if (refreshToken.getExpiresAt() == null ||
                !refreshToken.getExpiresAt()
                        .isAfter(OffsetDateTime.now())) {

            throw new BusinessException(
                    "Refresh token has expired",
                    401
            );
        }

        return refreshToken;
    }

    @Override
    @Transactional
    public void revokeToken(
            String rawToken
    ) {

        if (rawToken == null ||
                rawToken.isBlank()) {
            return;
        }

        String tokenHash =
                hashToken(rawToken);

        refreshTokenRepository
                .findByTokenHash(tokenHash)
                .ifPresent(token -> {

                    if (token.getRevokedAt() == null) {

                        token.setRevokedAt(
                                OffsetDateTime.now()
                        );

                        refreshTokenRepository.save(token);
                    }
                });
    }

    @Override
    @Transactional
    public void revokeAllUserTokens(
            User user
    ) {

        if (user == null ||
                user.getId() == null) {
            return;
        }

        refreshTokenRepository
                .revokeAllByUserId(
                        user.getId(),
                        OffsetDateTime.now()
                );
    }

    private String hashToken(
            String token
    ) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            token.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return Base64.getEncoder()
                    .encodeToString(hash);

        } catch (NoSuchAlgorithmException ex) {

            throw new IllegalStateException(
                    "SHA-256 algorithm not available",
                    ex
            );
        }
    }
}