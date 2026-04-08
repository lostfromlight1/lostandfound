package com.lostandfound.app.serviceimpl;

import com.lostandfound.app.dto.request.AuthRequest.*;
import com.lostandfound.app.dto.response.*;
import com.lostandfound.app.exception.*;
import com.lostandfound.app.model.*;
import com.lostandfound.app.repository.PasswordResetTokenRepository;
import com.lostandfound.app.repository.UserRepository;
import com.lostandfound.app.security.JwtService;
import com.lostandfound.app.service.AuthService;
import com.lostandfound.app.service.BaseService;
import com.lostandfound.app.service.EmailService;
import com.lostandfound.app.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl extends BaseService implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Value("${jwt.expiration.access-token}")
    private long jwtExpiration;

    @Value("${PUBLIC_FRONTEND_URL:http://localhost:3000}")
    private String frontendUrl;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        log.info("[{}] Registration attempt for email: {}", getTraceId(), request.email());

        if (userRepository.existsByEmail(request.email())) {
            log.warn("[{}] Registration failed: Email '{}' already exists", getTraceId(), request.email());
            throw new AppException(ErrorCode.EMAIL_ALREADY_IN_USE, "Email is already registered");
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .displayName(request.displayName())
                .contactInfo(request.contactInfo())
                .role(Role.USER)
                .provider(AuthProvider.LOCAL)
                .isLocked(false)
                .build();

        User savedUser = userRepository.save(user);
        log.info("[{}] User registered successfully with ID: {}", getTraceId(), savedUser.getId());

        return mapToResponse(savedUser);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("[{}] Login attempt for email: {}", getTraceId(), request.email());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));

        if (user.getProvider() == AuthProvider.GOOGLE) {
            log.warn("[{}] Login failed: User '{}' is a Google OAuth user", getTraceId(), request.email());
            throw new AppException(ErrorCode.AUTH_FAILED, "Looks like you signed up with Google. Please use the 'Login with Google' button.");
        }

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (BadCredentialsException ex) {
            log.warn("[{}] Login failed for '{}': Invalid credentials", getTraceId(), request.email());
            throw new AppException(ErrorCode.AUTH_FAILED, "Invalid email or password");
        }

        if (Boolean.TRUE.equals(user.getIsLocked())) {
            log.warn("[{}] Login blocked: User account '{}' is locked", getTraceId(), user.getEmail());
            throw new AppException(ErrorCode.USER_LOCKED, "This account has been locked");
        }

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getRawToken())
                .expiresIn(jwtExpiration)
                .user(mapToResponse(user))
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(TokenRefreshRequest request) {
        log.info("[{}] Refresh token request received", getTraceId());

        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(request.refreshToken());
        User user = newRefreshToken.getUser();

        if (Boolean.TRUE.equals(user.getIsLocked())) {
            refreshTokenService.revokeByUser(user.getId());
            throw new AppException(ErrorCode.USER_LOCKED, "This account has been locked");
        }

        String accessToken = jwtService.generateAccessToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken.getRawToken())
                .expiresIn(jwtExpiration)
                .user(mapToResponse(user))
                .build();
    }

    @Override
    @Transactional
    public void changePassword(User currentUser, ChangePasswordRequest request) {
        log.info("[{}] Changing password for user ID: {}", getTraceId(), currentUser.getId());

        User user = fetchUserById(currentUser.getId());

        if (user.getProvider() == AuthProvider.GOOGLE) {
            log.warn("[{}] Password change blocked: User ID {} is a Google user", getTraceId(), user.getId());
            throw new AppException(ErrorCode.BUSINESS_ERROR, "Google users cannot change their password here. Please manage your password via your Google Account.");
        }

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            log.warn("[{}] Password change failed: Incorrect old password for user ID: {}", getTraceId(), user.getId());
            throw new AppException(ErrorCode.BUSINESS_ERROR, "Incorrect current password");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        log.info("[{}] Password changed successfully for user ID: {}", getTraceId(), user.getId());
    }

    @Override
    @Transactional
    public void resetPassword(String email) {
        log.info("[{}] Password reset requested for email: {}", getTraceId(), email);

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            log.info("[{}] Password reset ignored: Email '{}' not found in system", getTraceId(), email);
            return;
        }

        if (user.getProvider() == AuthProvider.GOOGLE) {
            log.info("[{}] Password reset ignored: Email '{}' belongs to a Google OAuth user", getTraceId(), email);
            return;
        }

        passwordResetTokenRepository.deleteByUser_Id(user.getId());

        String resetToken = UUID.randomUUID().toString();

        PasswordResetToken tokenEntity = PasswordResetToken.builder()
                .token(resetToken)
                .user(user)
                .expiresAt(Instant.now().plus(15, ChronoUnit.MINUTES))
                .build();
        passwordResetTokenRepository.save(tokenEntity);

        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
        emailService.sendPasswordResetEmail(email, resetLink);

        log.info("[{}] Password reset link generated and saved to DB for user ID: {}", getTraceId(), user.getId());
    }

    @Override
    @Transactional
    public void confirmPasswordReset(String token, String newPassword) {
        log.info("[{}] Confirming password reset with token", getTraceId());

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("[{}] Password reset failed: Token not found", getTraceId());
                    return new AppException(ErrorCode.TOKEN_INVALID, "Invalid password reset token.");
                });

        if (resetToken.getExpiresAt().isBefore(Instant.now())) {
            log.warn("[{}] Password reset failed: Token expired for user ID: {}", getTraceId(), resetToken.getUser().getId());
            passwordResetTokenRepository.delete(resetToken); // Clean up the expired token
            throw new AppException(ErrorCode.TOKEN_EXPIRED, "This password reset link has expired. Please request a new one.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);

        log.info("[{}] Password successfully reset for user ID: {}", getTraceId(), user.getId());
    }

    // ------------------ Helpers ------------------

    private User fetchUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .contactInfo(user.getContactInfo())
                .role(user.getRole())
                .build();
    }
}