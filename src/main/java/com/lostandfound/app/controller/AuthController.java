package com.lostandfound.app.controller;

import com.lostandfound.app.annotation.ApiId;
import com.lostandfound.app.annotation.CheckSecurity;
import com.lostandfound.app.annotation.CurrentUser;
import com.lostandfound.app.dto.request.AuthRequest.*;
import com.lostandfound.app.dto.response.AuthResponse;
import com.lostandfound.app.dto.response.BaseResponse;
import com.lostandfound.app.dto.response.UserResponse;
import com.lostandfound.app.exception.AppException;
import com.lostandfound.app.exception.ErrorCode;
import com.lostandfound.app.model.User;
import com.lostandfound.app.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "1. Authentication", description = "Endpoints for user registration, login, and credential management")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @CheckSecurity.Public.canRead
    @ApiId("AUTH-001")
    @Operation(summary = "Register User")
    public ResponseEntity<BaseResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("REST request to register new user: {}", request.email());
        return BaseResponse.created("User registered successfully. Please verify your email.", authService.register(request));
    }

    @PostMapping("/login")
    @CheckSecurity.Public.canRead
    @ApiId("AUTH-002")
    @Operation(summary = "User Login", description = "Authenticates a user and returns JWT tokens in the JSON response.")
    public ResponseEntity<BaseResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("REST request to login user: {}", request.email());
        AuthResponse authData = authService.login(request);
        return BaseResponse.success("Login successful", authData);
    }

    @PostMapping("/google")
    @CheckSecurity.Public.canRead
    @ApiId("AUTH-010")
    @Operation(summary = "Google OAuth Login", description = "Authenticates a user via Google ID Token.")
    public ResponseEntity<BaseResponse<AuthResponse>> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        log.info("REST request for Google OAuth login");
        AuthResponse authData = authService.googleLogin(request.idToken());
        return BaseResponse.success("Google login successful", authData);
    }

    @PostMapping("/refresh")
    @CheckSecurity.Public.canRead
    @ApiId("AUTH-003")
    @Operation(summary = "Refresh Token", description = "Issues a new access token using a refresh token provided in the JSON body.")
    public ResponseEntity<BaseResponse<AuthResponse>> refreshToken(@RequestBody Map<String, String> requestBody) {
        log.info("REST request to refresh access token");

        String refreshTokenString = requestBody != null ? requestBody.get("refreshToken") : null;
        if (refreshTokenString == null || refreshTokenString.isBlank()) {
            throw new AppException(ErrorCode.AUTH_FAILED, "Refresh token is missing");
        }

        AuthResponse authData = authService.refreshToken(refreshTokenString);
        return BaseResponse.success("Token refreshed successfully", authData);
    }

    @PostMapping("/change-password")
    @ApiId("AUTH-004")
    @Operation(summary = "Change Password")
    public ResponseEntity<BaseResponse<Void>> changePassword(
            @Parameter(hidden = true) @CurrentUser User currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {
        log.info("REST request to change password for user ID: {}", currentUser != null ? currentUser.getId() : "UNKNOWN");
        authService.changePassword(currentUser, request);
        return BaseResponse.success("Password changed successfully");
    }

    @PostMapping("/reset-password")
    @CheckSecurity.Public.canRead
    @ApiId("AUTH-005")
    public ResponseEntity<BaseResponse<Void>> resetPassword(@RequestParam @Email @NotBlank String email) {
        log.info("REST request to trigger password reset for email: {}", email);
        authService.resetPassword(email);
        return BaseResponse.success("If the email exists, a reset link has been sent.");
    }

    @PostMapping("/reset-password/confirm")
    @CheckSecurity.Public.canRead
    @ApiId("AUTH-006")
    public ResponseEntity<BaseResponse<Void>> confirmPasswordReset(@Valid @RequestBody ConfirmPasswordResetRequest request) {
        log.info("REST request to confirm password reset");
        authService.confirmPasswordReset(request.token(), request.newPassword());
        return BaseResponse.success("Your password has been successfully reset. You can now log in.");
    }

    @GetMapping("/verify-email")
    @CheckSecurity.Public.canRead
    @ApiId("AUTH-007")
    public ResponseEntity<BaseResponse<Void>> verifyEmail(@RequestParam @NotBlank String token) {
        log.info("REST request to verify email");
        authService.verifyEmail(token);
        return BaseResponse.success("Email verified successfully. You can now log in.");
    }

    @PostMapping("/resend-verification")
    @CheckSecurity.Public.canRead
    @ApiId("AUTH-008")
    public ResponseEntity<BaseResponse<Void>> resendVerificationEmail(@RequestParam @Email @NotBlank String email) {
        log.info("REST request to resend verification email to: {}", email);
        authService.resendVerificationEmail(email);
        return BaseResponse.success("If the email is registered and unverified, a new code has been sent.");
    }

    @PostMapping("/logout")
    @ApiId("AUTH-009")
    @Operation(summary = "User Logout", description = "Revokes the refresh token provided in the JSON body.")
    public ResponseEntity<BaseResponse<Void>> logout(
            @Parameter(hidden = true) @CurrentUser User currentUser,
            @RequestBody(required = false) Map<String, String> requestBody) {

        log.info("REST request to logout user ID: {}", currentUser != null ? currentUser.getId() : "UNKNOWN");

        if (requestBody != null && requestBody.containsKey("refreshToken")) {
            authService.logout(requestBody.get("refreshToken"));
        }

        return BaseResponse.success("Successfully logged out");
    }
}