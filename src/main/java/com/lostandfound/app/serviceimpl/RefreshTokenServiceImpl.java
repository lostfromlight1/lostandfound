package com.lostandfound.app.serviceimpl;

import com.lostandfound.app.exception.AppException;
import com.lostandfound.app.exception.ErrorCode;
import com.lostandfound.app.model.RefreshToken;
import com.lostandfound.app.model.User;
import com.lostandfound.app.repository.RefreshTokenRepository;
import com.lostandfound.app.repository.UserRepository;
import com.lostandfound.app.service.BaseService; // <-- Added BaseService
import com.lostandfound.app.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl extends BaseService implements RefreshTokenService {

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
            log.error("[{}] SHA-256 algorithm missing from JVM", getTraceId(), e);
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Critical security error: Hashing algorithm unavailable");
        }
    }

    @Override
    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        log.info("[{}] Creating new refresh token for user ID: {}", getTraceId(), userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));

        String ipAddress = "UNKNOWN";
        String userAgent = "UNKNOWN";
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();
                ipAddress = req.getHeader("X-Forwarded-For");
                if (ipAddress == null || ipAddress.isEmpty()) ipAddress = req.getRemoteAddr();
                userAgent = req.getHeader("User-Agent");
            }
        } catch (Exception ignored) {
            log.warn("[{}] Could not extract request attributes for token metadata", getTraceId());
        }

        String rawToken = generateSecureToken();
        String hashedToken = hashToken(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(hashedToken)
                .expiresAt(Instant.now().plusMillis(refreshTokenDurationMs))
                .revoked(false)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        refreshTokenRepository.save(refreshToken);
        refreshToken.setRawToken(rawToken); // Keep raw token in memory to return to the user once

        log.info("[{}] Successfully generated and stored hashed refresh token for user ID: {}", getTraceId(), userId);
        return refreshToken;
    }

    @Override
    @Transactional
    public RefreshToken rotateRefreshToken(String tokenStr) {
        log.info("[{}] Attempting to rotate refresh token", getTraceId());

        String hashedIncomingToken = hashToken(tokenStr);
        RefreshToken token = refreshTokenRepository.findByToken(hashedIncomingToken)
                .orElseThrow(() -> new AppException(ErrorCode.TOKEN_INVALID, "Invalid refresh token"));

        if (token.isRevoked()) {
            if (token.getRevokedAt() != null && token.getRevokedAt().isAfter(Instant.now().minusSeconds(30))) {
                log.warn("[{}] Token reuse within grace period for user ID: {}. Issuing new pair.",
                        getTraceId(), token.getUser().getId());
                return createRefreshToken(token.getUser().getId());
            }

            log.error("[{}] REUSE DETECTED! Revoking all tokens for user: {}", getTraceId(), token.getUser().getId());
            refreshTokenRepository.revokeAllUserTokens(token.getUser().getId());
            throw new AppException(ErrorCode.TOKEN_INVALID, "Security breach detected. Please login again");
        }

        verifyExpiration(token);

        token.setRevoked(true);
        token.setRevokedAt(Instant.now());
        refreshTokenRepository.save(token);

        log.info("[{}] Old token revoked at {}. Issuing replacement for user ID: {}",
                getTraceId(), token.getRevokedAt(), token.getUser().getId());

        return createRefreshToken(token.getUser().getId());
    }

    @Override
    public void verifyExpiration(RefreshToken token) {
        if (token.getExpiresAt().isBefore(Instant.now())) {
            log.warn("[{}] Expired refresh token used for user ID: {}", getTraceId(), token.getUser().getId());
            refreshTokenRepository.delete(token);
            throw new AppException(ErrorCode.TOKEN_EXPIRED, "Session expired");
        }
    }

    @Override
    @Transactional
    public void revokeByUser(Long userId) {
        log.info("[{}] Revoking ALL refresh tokens for user ID: {}", getTraceId(), userId);
        refreshTokenRepository.revokeAllUserTokens(userId);
    }

    @Override
    @Transactional
    public void revokeToken(String tokenStr) {
        log.info("[{}] Revoking specific refresh token by string", getTraceId());
        String hashedToken = hashToken(tokenStr);
        refreshTokenRepository.findByToken(hashedToken).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            log.info("[{}] Successfully revoked target refresh token for user ID: {}", getTraceId(), token.getUser().getId());
        });
    }
}