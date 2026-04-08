package com.lostandfound.app.serviceimpl;

import com.lostandfound.app.dto.request.AuthRequest.*;
import com.lostandfound.app.dto.response.*;
import com.lostandfound.app.exception.*;
import com.lostandfound.app.model.*;
import com.lostandfound.app.repository.UserRepository;
import com.lostandfound.app.security.JwtService;
import com.lostandfound.app.service.AuthService;
import com.lostandfound.app.service.BaseService;
import com.lostandfound.app.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl extends BaseService implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    @Value("${jwt.expiration.access-token}")
    private long jwtExpiration;

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
                .role(Role.USER) // Default role
                .isLocked(false)
                .build();

        User savedUser = userRepository.save(user);
        log.info("[{}] User registered successfully with ID: {}", getTraceId(), savedUser.getId());

        return mapToResponse(savedUser);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("[{}] Login attempt for email: {}", getTraceId(), request.email());

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (BadCredentialsException ex) {
            log.warn("[{}] Login failed for '{}': Invalid credentials", getTraceId(), request.email());
            throw new AppException(ErrorCode.AUTH_FAILED, "Invalid email or password");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));

        if (Boolean.TRUE.equals(user.getIsLocked())) {
            log.warn("[{}] Login blocked: User account '{}' is locked", getTraceId(), user.getEmail());
            throw new AppException(ErrorCode.USER_LOCKED, "This account has been locked");
        }

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId()); // ADDED

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getRawToken())
                .expiresIn(jwtExpiration)
                .user(mapToResponse(user))
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(TokenRefreshRequest request){
        log.info("[{}] Refresh token request received", getTraceId());

        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(request.refreshToken());
        User user = newRefreshToken.getUser();

        if(Boolean.TRUE.equals(user.getIsLocked())){
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

        // Always fetch a fresh entity from the DB to ensure we aren't working with stale detached data
        User user = fetchUserById(currentUser.getId());

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
            // Security Best Practice: Don't reveal if an email exists during a password reset request.
            log.info("[{}] Password reset ignored: Email '{}' not found in system", getTraceId(), email);
            return;
        }

        // TODO: Implement email sending logic here (e.g., generate a short-lived token, save it to DB, and email a link)
        log.info("[{}] Password reset link generated for user ID: {}", getTraceId(), user.getId());
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