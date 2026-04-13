package com.lostandfound.app.controller;

import com.lostandfound.app.annotation.ApiId;
import com.lostandfound.app.annotation.CheckSecurity;
import com.lostandfound.app.annotation.CurrentUser;
import com.lostandfound.app.dto.request.AuthRequest.ChangePasswordRequest;
import com.lostandfound.app.dto.request.AuthRequest.ConfirmPasswordResetRequest;
import com.lostandfound.app.dto.request.AuthRequest.GoogleLoginRequest;
import com.lostandfound.app.dto.request.AuthRequest.LoginRequest;
import com.lostandfound.app.dto.request.AuthRequest.RegisterRequest;
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
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "1. Authentication", description = "Endpoints for user registration, login, and credential management")
public class AuthController {

    private final AuthService authService;

    @Value("${jwt.expiration.access-token}")
    private long accessTokenDurationMs;

    @Value("${jwt.expiration.refresh-token}")
    private long refreshTokenDurationMs;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Value("${app.cookie.domain:}")
    private String cookieDomain;

    @PostMapping("/register")
    @CheckSecurity.Public.canRead
    @ApiId("AUTH-001")
    @Operation(summary = "Register User", description = "Registers a new user and sends a verification email.")
    public ResponseEntity<BaseResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("REST request to register new user: {}", request.email());
        return BaseResponse.created("User registered successfully. Please verify your email.", authService.register(request));
    }

    @PostMapping("/login")
    @CheckSecurity.Public.canRead
    @ApiId("AUTH-002")
    @Operation(summary = "User Login", description = "Authenticates a user and sets HttpOnly token cookies.")
    public ResponseEntity<BaseResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        log.info("REST request to login user: {}", request.email());
        AuthResponse authData = authService.login(request);
        setTokenCookies(response, authData.accessToken(), authData.refreshToken());

        return BaseResponse.success("Login successful", authData);
    }

    @PostMapping("/google")
    @CheckSecurity.Public.canRead
    @ApiId("AUTH-010")
    @Operation(summary = "Google OAuth Login", description = "Authenticates a user via Google ID Token.")
    public ResponseEntity<BaseResponse<AuthResponse>> googleLogin(
            @Valid @RequestBody GoogleLoginRequest request,
            HttpServletResponse response) {

        log.info("REST request for Google OAuth login");
        AuthResponse authData = authService.googleLogin(request.idToken());
        setTokenCookies(response, authData.accessToken(), authData.refreshToken());

        return BaseResponse.success("Google login successful", authData);
    }

    @PostMapping("/refresh")
    @CheckSecurity.Public.canRead
    @ApiId("AUTH-003")
    @Operation(summary = "Refresh Token", description = "Issues a new access token using a valid HttpOnly refresh cookie.")
    public ResponseEntity<BaseResponse<AuthResponse>> refreshToken(
            @CookieValue(name = "app_refresh_token", required = false) String refreshTokenString,
            HttpServletResponse response) {

        log.info("REST request to refresh access token");
        if (refreshTokenString == null || refreshTokenString.isBlank()) {
            throw new AppException(ErrorCode.AUTH_FAILED, "Refresh token is missing or expired");
        }

        AuthResponse authData = authService.refreshToken(refreshTokenString);
        setTokenCookies(response, authData.accessToken(), authData.refreshToken());

        return BaseResponse.success("Token refreshed successfully", authData);
    }

    @PostMapping("/change-password")
    @ApiId("AUTH-004")
    @Operation(summary = "Change Password", description = "Allows an authenticated user to change their password.")
    public ResponseEntity<BaseResponse<Void>> changePassword(
            @Parameter(hidden = true) @CurrentUser User currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {

        log.info("REST request to change password for user ID: {}", currentUser != null ? currentUser.getId() : "UNKNOWN");
        if (currentUser == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Access Denied: Missing or invalid authentication token.");
        }

        authService.changePassword(currentUser, request);
        return BaseResponse.success("Password changed successfully");
    }

    @PostMapping("/reset-password")
    @CheckSecurity.Public.canRead
    @ApiId("AUTH-005")
    @Operation(summary = "Request Password Reset", description = "Sends a password reset link to the provided email.")
    public ResponseEntity<BaseResponse<Void>> resetPassword(@RequestParam @Email @NotBlank String email) {
        log.info("REST request to trigger password reset for email: {}", email);
        authService.resetPassword(email);
        return BaseResponse.success("If the email exists, a reset link has been sent.");
    }

    @PostMapping("/reset-password/confirm")
    @CheckSecurity.Public.canRead
    @ApiId("AUTH-006")
    @Operation(summary = "Confirm Password Reset", description = "Resets the password using a valid reset token.")
    public ResponseEntity<BaseResponse<Void>> confirmPasswordReset(@Valid @RequestBody ConfirmPasswordResetRequest request) {
        log.info("REST request to confirm password reset");
        authService.confirmPasswordReset(request.token(), request.newPassword());
        return BaseResponse.success("Your password has been successfully reset. You can now log in.");
    }

    @GetMapping("/verify-email")
    @CheckSecurity.Public.canRead
    @ApiId("AUTH-007")
    @Operation(summary = "Verify Email", description = "Verifies a user's email address using a token.")
    public ResponseEntity<BaseResponse<Void>> verifyEmail(@RequestParam @NotBlank String token) {
        log.info("REST request to verify email");
        authService.verifyEmail(token);
        return BaseResponse.success("Email verified successfully. You can now log in.");
    }

    @PostMapping("/resend-verification")
    @CheckSecurity.Public.canRead
    @ApiId("AUTH-008")
    @Operation(summary = "Resend Verification Email", description = "Resends the email verification link.")
    public ResponseEntity<BaseResponse<Void>> resendVerificationEmail(@RequestParam @Email @NotBlank String email) {
        log.info("REST request to resend verification email to: {}", email);
        authService.resendVerificationEmail(email);
        return BaseResponse.success("If the email is registered and unverified, a new code has been sent.");
    }

    @PostMapping("/logout")
    @ApiId("AUTH-009")
    @Operation(summary = "User Logout", description = "Revokes refresh token and clears HttpOnly cookies.")
    public ResponseEntity<BaseResponse<Void>> logout(
            @Parameter(hidden = true) @CurrentUser User currentUser,
            @CookieValue(name = "app_refresh_token", required = false) String refreshTokenString,
            HttpServletResponse response) {

        log.info("REST request to logout user ID: {}", currentUser != null ? currentUser.getId() : "UNKNOWN");
        if (refreshTokenString != null && !refreshTokenString.isBlank()) {
            authService.logout(refreshTokenString);
        }

        boolean isProduction = "prod".equalsIgnoreCase(activeProfile);
        String sameSitePolicy = isProduction ? "None" : "Lax";

        ResponseCookie.ResponseCookieBuilder clearAccessBuilder = ResponseCookie.from("app_access_token", "")
                .httpOnly(true)
                .secure(isProduction)
                .path("/")
                .sameSite(sameSitePolicy)
                .maxAge(0);

        ResponseCookie.ResponseCookieBuilder clearRefreshBuilder = ResponseCookie.from("app_refresh_token", "")
                .httpOnly(true)
                .secure(isProduction)
                .path("/")
                .sameSite(sameSitePolicy)
                .maxAge(0);

        if (isProduction && cookieDomain != null && !cookieDomain.isBlank()) {
            clearAccessBuilder.domain(cookieDomain);
            clearRefreshBuilder.domain(cookieDomain);
        }

        response.addHeader(HttpHeaders.SET_COOKIE, clearAccessBuilder.build().toString());
        response.addHeader(HttpHeaders.SET_COOKIE, clearRefreshBuilder.build().toString());

        return BaseResponse.success("Successfully logged out");
    }

    private void setTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        boolean isProduction = "prod".equalsIgnoreCase(activeProfile);
        String sameSitePolicy = isProduction ? "None" : "Lax";

        ResponseCookie.ResponseCookieBuilder accessCookieBuilder = ResponseCookie.from("app_access_token", accessToken)
                .httpOnly(true)
                .secure(isProduction)
                .path("/")
                .maxAge(accessTokenDurationMs / 1000)
                .sameSite(sameSitePolicy);

        ResponseCookie.ResponseCookieBuilder refreshCookieBuilder = ResponseCookie.from("app_refresh_token", refreshToken)
                .httpOnly(true)
                .secure(isProduction)
                .path("/")
                .maxAge(refreshTokenDurationMs / 1000)
                .sameSite(sameSitePolicy);

        if (isProduction && cookieDomain != null && !cookieDomain.isBlank()) {
            accessCookieBuilder.domain(cookieDomain);
            refreshCookieBuilder.domain(cookieDomain);
        }

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookieBuilder.build().toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookieBuilder.build().toString());
    }
}