package com.lostandfound.app.controller;

import com.lostandfound.app.annotation.ApiId;
import com.lostandfound.app.dto.request.AuthRequest.ChangePasswordRequest;
import com.lostandfound.app.dto.request.AuthRequest.ConfirmPasswordResetRequest;
import com.lostandfound.app.dto.request.AuthRequest.LoginRequest;
import com.lostandfound.app.dto.request.AuthRequest.RegisterRequest;
import com.lostandfound.app.dto.response.AuthResponse;
import com.lostandfound.app.dto.response.BaseResponse;
import com.lostandfound.app.dto.response.UserResponse;
import com.lostandfound.app.dto.request.AuthRequest.TokenRefreshRequest;
import com.lostandfound.app.model.User;
import com.lostandfound.app.annotation.CheckSecurity;
import com.lostandfound.app.annotation.CurrentUser;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;


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

    @PostMapping("/register")
    @CheckSecurity.Public.canRead
    @ApiId("AUTH-001")
    public ResponseEntity<BaseResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return BaseResponse.created("User registered successfully. Please verify your email.", authService.register(request));
    }

    @PostMapping("/login")
    @CheckSecurity.Public.canRead
    @ApiId("AUTH-002")
    public ResponseEntity<BaseResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        AuthResponse authData = authService.login(request);
        setTokenCookies(response, authData.accessToken(), authData.refreshToken());

        return BaseResponse.success("Login successful", authData);
    }

    @PostMapping("/refresh")
    @CheckSecurity.Public.canRead
    @ApiId("AUTH-003")
    public ResponseEntity<BaseResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request,
            HttpServletResponse response) {

        AuthResponse authData = authService.refreshToken(request);
        setTokenCookies(response, authData.accessToken(), authData.refreshToken());

        return BaseResponse.success("Token refreshed successfully", authData);
    }

    @PostMapping("/change-password")
    @ApiId("AUTH-004")
    public ResponseEntity<BaseResponse<Void>> changePassword(
            @Parameter(hidden = true) @CurrentUser User currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(currentUser, request);
        return BaseResponse.success("Password changed successfully");
    }

    @PostMapping("/reset-password")
    @CheckSecurity.Public.canRead
    @ApiId("AUTH-005")
    public ResponseEntity<BaseResponse<Void>> resetPassword(@RequestParam @Email @NotBlank String email) {
        authService.resetPassword(email);
        return BaseResponse.success("If the email exists, a reset link has been sent.");
    }

    @PostMapping("/reset-password/confirm")
    @CheckSecurity.Public.canRead
    @ApiId("AUTH-006")
    public ResponseEntity<BaseResponse<Void>> confirmPasswordReset(@Valid @RequestBody ConfirmPasswordResetRequest request) {
        authService.confirmPasswordReset(request.token(), request.newPassword());
        return BaseResponse.success("Your password has been successfully reset. You can now log in.");
    }

    @GetMapping("/verify-email")
    @CheckSecurity.Public.canRead
    @ApiId("AUTH-007")
    public ResponseEntity<BaseResponse<Void>> verifyEmail(@RequestParam @NotBlank String token) {
        authService.verifyEmail(token);
        return BaseResponse.success("Email verified successfully. You can now log in.");
    }

    @PostMapping("/resend-verification")
    @CheckSecurity.Public.canRead
    @ApiId("AUTH-008")
    public ResponseEntity<BaseResponse<Void>> resendVerificationEmail(@RequestParam @Email @NotBlank String email) {
        authService.resendVerificationEmail(email);
        return BaseResponse.success("If the email is registered and unverified, a new link has been sent.");
    }

    @PostMapping("/logout")
    @ApiId("AUTH-009")
    @Operation(summary = "User Logout", description = "Revokes refresh token and clears HttpOnly cookies.")
    public ResponseEntity<BaseResponse<Void>> logout(
            @Parameter(hidden = true) @CurrentUser User currentUser,
            @CookieValue(name = "refreshToken", required = false) String refreshTokenString,
            HttpServletResponse response) {

        if (refreshTokenString != null && !refreshTokenString.isBlank()) {
            authService.logout(refreshTokenString);
        }

        ResponseCookie clearAccess = ResponseCookie.from("accessToken", "")
                .httpOnly(true).secure(true).path("/").maxAge(0).build();
        ResponseCookie clearRefresh = ResponseCookie.from("refreshToken", "")
                .httpOnly(true).secure(true).path("/").maxAge(0).build();

        response.addHeader(HttpHeaders.SET_COOKIE, clearAccess.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, clearRefresh.toString());

        return BaseResponse.success("Successfully logged out");
    }
    // ---------------------- Helper Method ----------------------

    private void setTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(accessTokenDurationMs / 1000)
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenDurationMs / 1000)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }
}