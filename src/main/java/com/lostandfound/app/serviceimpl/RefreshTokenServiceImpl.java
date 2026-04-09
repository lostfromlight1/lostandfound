package com.lostandfound.app.serviceimpl;

import com.lostandfound.app.exception.AppException;
import com.lostandfound.app.exception.ErrorCode;
import com.lostandfound.app.model.RefreshToken;
import com.lostandfound.app.model.User;
import com.lostandfound.app.repository.RefreshTokenRepository;
import com.lostandfound.app.repository.UserRepository;
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
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
        } catch (Exception ignored) {}

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
        refreshToken.setRawToken(rawToken);
        return refreshToken;
    }

    @Override
    @Transactional
    public RefreshToken rotateRefreshToken(String tokenStr) {
        String hashedIncomingToken = hashToken(tokenStr);
        RefreshToken token = refreshTokenRepository.findByToken(hashedIncomingToken)
                .orElseThrow(() -> new AppException(ErrorCode.TOKEN_INVALID, "Invalid refresh token"));

        if(token.isRevoked()){
            log.error("[{}] REUSE DETECTED! Revoking all tokens for user: {}", MDC.get("traceId"), token.getUser().getId());
            refreshTokenRepository.revokeAllUserTokens(token.getUser().getId());
            throw new AppException(ErrorCode.TOKEN_INVALID, "Security breach detected. Please login again");
        }

        verifyExpiration(token);
        token.setRevoked(true);
        refreshTokenRepository.save(token);
        return createRefreshToken(token.getUser().getId());
    }

    @Override
    public void verifyExpiration(RefreshToken token) {
        if (token.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new AppException(ErrorCode.TOKEN_EXPIRED, "Session expired");
        }
    }

    @Override
    @Transactional
    public void revokeByUser(Long userId) {
        refreshTokenRepository.revokeAllUserTokens(userId);
    }

    @Override
    @Transactional
    public void revokeToken(String tokenStr) {
        String hashedToken = hashToken(tokenStr);
        refreshTokenRepository.findByToken(hashedToken).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }
}