package com.lostandfound.app.serviceimpl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.lostandfound.app.dto.request.AuthRequest.*;
import com.lostandfound.app.dto.response.*;
import com.lostandfound.app.exception.*;
import com.lostandfound.app.model.*;
import com.lostandfound.app.repository.PasswordResetTokenRepository;
import com.lostandfound.app.repository.UserRepository;
import com.lostandfound.app.repository.VerificationTokenRepository;
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

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;
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
    private final VerificationTokenRepository verificationTokenRepository;

    @Value("${jwt.expiration.access-token}")
    private long jwtExpiration;

    @Value("${PUBLIC_FRONTEND_URL:http://localhost:3000}")
    private String frontendUrl;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    private String generateVerificationCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder(6);
        SecureRandom rnd = new SecureRandom();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return code.toString();
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        log.info("[{}] Attempting to register new user: {}", getTraceId(), request.email());
        if (userRepository.existsByEmail(request.email())) {
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
                .emailVerified(false)
                .build();

        User savedUser = userRepository.save(user);

        String token = generateVerificationCode();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(savedUser)
                .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .build();
        verificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(savedUser.getEmail(), token);
        log.info("[{}] Registration successful. Verification email sent to: {}", getTraceId(), savedUser.getEmail());
        return mapToResponse(savedUser);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("[{}] Attempting login for email: {}", getTraceId(), request.email());
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AppException(ErrorCode.AUTH_FAILED, "Invalid email or password"));

        if (user.getProvider() == AuthProvider.GOOGLE) {
            throw new AppException(ErrorCode.AUTH_FAILED, "Looks like you signed up with Google. Please use the 'Login with Google' button.");
        }

        if (!user.isEmailVerified()) {
            throw new AppException(ErrorCode.AUTH_FAILED, "Please verify your email address before logging in. Check your inbox.");
        }

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (BadCredentialsException ex) {
            throw new AppException(ErrorCode.AUTH_FAILED, "Invalid email or password");
        }

        if (Boolean.TRUE.equals(user.getIsLocked())) {
            throw new AppException(ErrorCode.USER_LOCKED, "This account has been locked");
        }

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        log.info("[{}] User logged in successfully: {} (Role: {})", getTraceId(), user.getEmail(), user.getRole());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getRawToken())
                .expiresIn(jwtExpiration)
                .user(mapToResponse(user))
                .build();
    }

    @Override
    @Transactional
    public AuthResponse googleLogin(String idTokenString) {
        log.info("[{}] Attempting Google OAuth login", getTraceId());
        try {
            HttpTransport transport = new NetHttpTransport();
            JsonFactory jsonFactory = new GsonFactory();

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new AppException(ErrorCode.AUTH_FAILED, "Invalid Google ID token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String providerId = payload.getSubject();

            String pictureUrl = (String) payload.get("picture");

            Optional<User> userOptional = userRepository.findByEmail(email);
            User user;

            if (userOptional.isPresent()) {
                user = userOptional.get();
                if (user.getProvider() != AuthProvider.LOCAL && user.getProvider() != AuthProvider.GOOGLE) {
                    throw new AppException(ErrorCode.AUTH_FAILED, "Account collision detected.");
                } else if (user.getProvider() == AuthProvider.LOCAL) {
                    throw new AppException(ErrorCode.AUTH_FAILED, "Email already registered with a password. Please login normally.");
                }

                if (user.getAvatarUrl() == null && pictureUrl != null) {
                    user.setAvatarUrl(pictureUrl);
                    userRepository.save(user);
                }
            } else {
                user = User.builder()
                        .email(email)
                        .displayName(name)
                        .provider(AuthProvider.GOOGLE)
                        .providerId(providerId)
                        .avatarUrl(pictureUrl)
                        .emailVerified(true)
                        .isLocked(false)
                        .role(Role.USER)
                        .build();
                user = userRepository.save(user);
                log.info("[{}] Created new user from Google OAuth: {}", getTraceId(), email);
            }

            if (Boolean.TRUE.equals(user.getIsLocked())) {
                throw new AppException(ErrorCode.USER_LOCKED, "This account has been locked");
            }

            refreshTokenService.revokeByUser(user.getId());
            String accessToken = jwtService.generateAccessToken(user);
            RefreshToken refreshTokenEntity = refreshTokenService.createRefreshToken(user.getId());

            log.info("[{}] Google login successful for: {}", getTraceId(), email);
            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshTokenEntity.getRawToken())
                    .expiresIn(jwtExpiration)
                    .user(mapToResponse(user))
                    .build();

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("[{}] Google verification failed", getTraceId(), e);
            throw new AppException(ErrorCode.AUTH_FAILED, "Could not verify Google account");
        }
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshTokenStr) {
        log.info("[{}] Attempting to refresh token", getTraceId());
        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(refreshTokenStr);
        User user = newRefreshToken.getUser();

        if (Boolean.TRUE.equals(user.getIsLocked())) {
            refreshTokenService.revokeByUser(user.getId());
            throw new AppException(ErrorCode.USER_LOCKED, "This account has been locked");
        }

        String accessToken = jwtService.generateAccessToken(user);

        log.info("[{}] Token successfully refreshed for user: {}", getTraceId(), user.getEmail());
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
        if (currentUser == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Access Denied: Missing or invalid authentication token.");
        }

        User user = fetchUserById(currentUser.getId());

        if (user.getProvider() == AuthProvider.GOOGLE) {
            throw new AppException(ErrorCode.BUSINESS_ERROR, "Google users cannot change their password here.");
        }

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.BUSINESS_ERROR, "Incorrect current password");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        log.info("[{}] Password successfully changed for user ID: {}", getTraceId(), user.getId());
    }

    @Override
    @Transactional
    public void resetPassword(String email) {
        log.info("[{}] Processing password reset request for email: {}", getTraceId(), email);
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || user.getProvider() == AuthProvider.GOOGLE) return;

        passwordResetTokenRepository.deleteByUser_Id(user.getId());
        String resetToken = UUID.randomUUID().toString();

        PasswordResetToken tokenEntity = PasswordResetToken.builder()
                .token(resetToken)
                .user(user)
                .expiresAt(Instant.now().plus(15, ChronoUnit.MINUTES))
                .build();
        passwordResetTokenRepository.save(tokenEntity);
        emailService.sendPasswordResetEmail(email, frontendUrl + "/reset-password?token=" + resetToken);
    }

    @Override
    @Transactional
    public void confirmPasswordReset(String token, String newPassword) {
        log.info("[{}] Processing password reset confirmation", getTraceId());
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.TOKEN_INVALID, "Invalid password reset token."));

        if (resetToken.getExpiresAt().isBefore(Instant.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new AppException(ErrorCode.TOKEN_EXPIRED, "This password reset link has expired.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken);
        log.info("[{}] Password successfully reset via token for user ID: {}", getTraceId(), user.getId());
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        try {
            refreshTokenService.revokeToken(refreshToken);
            log.info("[{}] User logged out and token revoked", getTraceId());
        } catch (Exception e) {
            log.warn("[{}] Error revoking token during logout: {}", getTraceId(), e.getMessage());
        }
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        log.info("[{}] Attempting to verify email via token", getTraceId());
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.TOKEN_INVALID, "Invalid verification token."));

        if (verificationToken.getExpiresAt().isBefore(Instant.now())) {
            verificationTokenRepository.delete(verificationToken);
            throw new AppException(ErrorCode.TOKEN_EXPIRED, "This verification link has expired. Please request a new one.");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        verificationTokenRepository.delete(verificationToken);
        log.info("[{}] Email verified successfully for user ID: {}", getTraceId(), user.getId());
    }

    @Override
    @Transactional
    public void resendVerificationEmail(String email) {
        log.info("[{}] Attempting to resend verification email for: {}", getTraceId(), email);
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null || user.isEmailVerified() || user.getProvider() == AuthProvider.GOOGLE) {
            return;
        }

        verificationTokenRepository.deleteByUser_Id(user.getId());

        String token = generateVerificationCode();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .build();
        verificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(user.getEmail(), token);
        log.info("[{}] Resent verification email to: {}", getTraceId(), email);
    }

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
                .avatarUrl(user.getAvatarUrl()) // <-- NEW!
                .build();
    }
}