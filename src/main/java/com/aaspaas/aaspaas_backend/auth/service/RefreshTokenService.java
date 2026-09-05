package com.aaspaas.aaspaas_backend.auth.service;

import com.aaspaas.aaspaas_backend.auth.entity.RefreshToken;
import com.aaspaas.aaspaas_backend.user.entity.User;

public interface RefreshTokenService {

    String createRefreshToken(User user);

    RefreshToken verifyRefreshToken(String rawToken);

    void revokeToken(String rawToken);

    void revokeAllUserTokens(User user);
}