package com.lostandfound.app.service;

import com.lostandfound.app.model.RefreshToken;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(Long userId);

    RefreshToken rotateRefreshToken(String tokenStr);

    void verifyExpiration(RefreshToken token);

    void revokeByUser(Long userId);
}