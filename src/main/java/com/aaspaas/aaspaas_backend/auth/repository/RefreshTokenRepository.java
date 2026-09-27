package com.aaspaas.aaspaas_backend.auth.repository;

import com.aaspaas.aaspaas_backend.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(
            String tokenHash
    );

    @Modifying
    @Query("""
            UPDATE RefreshToken rt
            SET rt.revokedAt = :revokedAt
            WHERE rt.user.id = :userId
              AND rt.revokedAt IS NULL
            """)
    int revokeAllByUserId(
            @Param("userId") Long userId,
            @Param("revokedAt") OffsetDateTime revokedAt
    );
}