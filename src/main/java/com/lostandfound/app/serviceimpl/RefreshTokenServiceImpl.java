package com.lostandfound.app.serviceimpl;

import com.lostandfound.app.exception.AppException;
import com.lostandfound.app.exception.ErrorCode;
import com.lostandfound.app.model.RefreshToken;
import com.lostandfound.app.model.User;
import com.lostandfound.app.repository.RefreshTokenRepository;
import com.lostandfound.app.repository.UserRepository;
import com.lostandfound.app.service.RefreshTokenService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${jwt.expiration.refresh-token}")
    private long refreshTokenDurationMs;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private String generateSecureToken(){
        byte[] randomBytes = new byte[64];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String hashToken(String token){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm missing from JVM", e);
            throw new AppException(ErrorCode.BUSINESS_ERROR, "Security error");
        }
    }

    @Override
    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        log.info("[{}] Creating refresh token for user: {}", MDC.get("traceId"), userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));

        String rawToken = generateSecureToken();
        String hashedToken = hashToken(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(hashedToken)
                .expiresAt(Instant.now().plusMillis(refreshTokenDurationMs))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        refreshToken.setRawToken(rawToken);

        log.info("[{}] Refresh token created successfully for user: {}", MDC.get("traceId"), userId);

        return refreshToken;
    }

    @Override
    @Transactional
    public RefreshToken rotateRefreshToken(String tokenStr) {
        String hashedIncomingToken = hashToken(tokenStr);

        RefreshToken token  =  refreshTokenRepository.findByToken(hashedIncomingToken)
                .orElseThrow(() -> new AppException(ErrorCode.TOKEN_INVALID, "Invalid refresh token"));

        if(token.isRevoked()){
            log.error("[{}] REUSE DETECTED! Revoking all tokens for user: {}", MDC.get("traceId"), token.getUser().getId());
            refreshTokenRepository.revokeAllUserTokens(token.getUser().getId());
            throw new AppException(ErrorCode.TOKEN_INVALID, "Security breach detected. Please login again");
        }

        verifyExpiration(token);

        token.setRevoked(true);
        refreshTokenRepository.save(token);

        log.debug("[{}] Token rotated successfully", MDC.get("traceId"));
        return createRefreshToken(token.getUser().getId());
    }

    @Override
    public void verifyExpiration(RefreshToken token) {
        if (token.getExpiresAt().isBefore(Instant.now())) {
            log.info("[{}] Token {} expired, purging", MDC.get("traceId"), token.getId());
            refreshTokenRepository.delete(token);
            throw new AppException(ErrorCode.TOKEN_EXPIRED, "Session expired");
        }
    }

    @Override
    @Transactional
    public void revokeByUser(Long userId) {
        log.info("[{}] Revoking all sessions for user: {}", MDC.get("traceId"), userId);
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User not found");
        }
        refreshTokenRepository.revokeAllUserTokens(userId);
    }
}