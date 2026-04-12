package com.lostandfound.app.dto.response;

import lombok.Builder;

@Builder
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UserResponse user
) {
    public AuthResponse {
        if (tokenType == null) tokenType = "Bearer";
    }
}