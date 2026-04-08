package com.lostandfound.app.controller;

import com.lostandfound.app.dto.request.AuthRequest.ChangePasswordRequest;
import com.lostandfound.app.dto.request.AuthRequest.LoginRequest;
import com.lostandfound.app.dto.request.AuthRequest.RegisterRequest;
import com.lostandfound.app.dto.response.AuthResponse;
import com.lostandfound.app.dto.response.BaseResponse;
import com.lostandfound.app.dto.response.UserResponse;
import com.lostandfound.app.dto.request.AuthRequest.TokenRefreshRequest;
import com.lostandfound.app.model.User;
import com.lostandfound.app.security.CheckSecurity;
import com.lostandfound.app.security.CurrentUser;
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

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "1. Authentication", description = "Endpoints for user registration, login, and credential management")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @CheckSecurity.Public.canRead
    @Operation(summary = "Register a new user", description = "Creates a new user account. Returns the public profile data.")
    public ResponseEntity<BaseResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        log.info("Received registration request for email: {}", request.email());
        UserResponse response = authService.register(request);
        return BaseResponse.created("User registered successfully", response);
    }

    @PostMapping("/login")
    @CheckSecurity.Public.canRead
    @Operation(summary = "User Login", description = "Authenticates a user and returns a JWT access token.")
    public ResponseEntity<BaseResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        log.info("Received login request for email: {}", request.email());
        AuthResponse response = authService.login(request);
        return BaseResponse.success("Login successful", response);
    }

    @PostMapping("/refresh")
    @CheckSecurity.Public.canRead
    @Operation(summary = "Refresh Token", description = "Get a new access token using a valid refresh token.")
    public ResponseEntity<BaseResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request) {
        log.info("Received refresh token request");
        AuthResponse response = authService.refreshToken(request);
        return BaseResponse.success("Token refreshed successfully", response);
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change Password", description = "Allows an authenticated user to change their password.")
    public ResponseEntity<BaseResponse<Void>> changePassword(
            @Parameter(hidden = true) @CurrentUser User currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {
        log.info("Received password change request for user ID: {}", currentUser.getId());
        authService.changePassword(currentUser, request);
        return BaseResponse.success("Password changed successfully");
    }

    @PostMapping("/reset-password")
    @CheckSecurity.Public.canRead
    @Operation(summary = "Request Password Reset", description = "Initiates the password reset flow for a given email address.")
    public ResponseEntity<BaseResponse<Void>> resetPassword(
            @RequestParam @Email @NotBlank String email) {
        log.info("Received password reset request for email: {}", email);
        authService.resetPassword(email);
        return BaseResponse.success("If the email exists, a reset link has been sent.");
    }
}