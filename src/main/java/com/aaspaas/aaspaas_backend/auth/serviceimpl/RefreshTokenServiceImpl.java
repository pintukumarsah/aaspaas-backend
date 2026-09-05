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

    private final RefreshTokenRepository refreshTokenRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public String createRefreshToken(User user) {

        byte[] randomBytes = new byte[32];

        secureRandom.nextBytes(randomBytes);

        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(user);
        refreshToken.setTokenHash(hashToken(rawToken));
        refreshToken.setExpiresAt(
                OffsetDateTime.now().plusDays(7)
        );

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Override
    public RefreshToken verifyRefreshToken(String rawToken) {

        String tokenHash = hashToken(rawToken);

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

        if (refreshToken.getExpiresAt()
                .isBefore(OffsetDateTime.now())) {

            throw new BusinessException(
                    "Refresh token has expired",
                    401
            );
        }

        return refreshToken;
    }

    @Override
    @Transactional
    public void revokeToken(String rawToken) {

        String tokenHash = hashToken(rawToken);

        refreshTokenRepository
                .findByTokenHash(tokenHash)
                .ifPresent(token -> {
                    token.setRevokedAt(
                            OffsetDateTime.now()
                    );

                    refreshTokenRepository.save(token);
                });
    }

    @Override
    @Transactional
    public void revokeAllUserTokens(User user) {

        // Individual tokens can be revoked as needed.
        // Bulk revocation can be added later.
    }

    private String hashToken(String token) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            token.getBytes(StandardCharsets.UTF_8)
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