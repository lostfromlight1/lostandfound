// src/main/java/com/lostandfound/app/serviceimpl/AuthServiceImpl.java

package com.lostandfound.app.serviceimpl;

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
    private final VerificationTokenRepository verificationTokenRepository;

    @Value("${jwt.expiration.access-token}")
    private long jwtExpiration;

    @Value("${PUBLIC_FRONTEND_URL:http://localhost:3000}")
    private String frontendUrl;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
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

        // Verification Flow
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(savedUser)
                .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .build();
        verificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(savedUser.getEmail(), frontendUrl + "/verify-email?token=" + token);
        return mapToResponse(savedUser);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));

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

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getRawToken())
                .expiresIn(jwtExpiration)
                .user(mapToResponse(user))
                .build();
    }

    // FIX: Method parameter changed from TokenRefreshRequest object to String
    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshTokenStr) {
        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(refreshTokenStr);
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
        User user = fetchUserById(currentUser.getId());

        if (user.getProvider() == AuthProvider.GOOGLE) {
            throw new AppException(ErrorCode.BUSINESS_ERROR, "Google users cannot change their password here.");
        }

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.BUSINESS_ERROR, "Incorrect current password");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetPassword(String email) {
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
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        try {
            refreshTokenService.revokeToken(refreshToken);
        } catch (Exception e) {
            log.warn("Error revoking token during logout: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
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
    }

    @Override
    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));

        if (user.isEmailVerified() || user.getProvider() == AuthProvider.GOOGLE) return;

        verificationTokenRepository.deleteByUser_Id(user.getId());

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .build();
        verificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(user.getEmail(), frontendUrl + "/verify-email?token=" + token);
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
                .build();
    }
}