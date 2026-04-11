package com.lostandfound.app.dto.request;

import jakarta.validation.constraints.*;

public class AuthRequest {

    public record RegisterRequest(
            @NotBlank(message = "Email is required")
            @Email(message = "Invalid email format")
            @Size(max = 255) String email,

            @NotBlank(message = "Password is required")
            @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
            @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
                    message = "Password must contain uppercase, lowercase, number, and special character")
            String password,

            @NotBlank(message = "Display name is required")
            @Size(max = 100) String displayName,

            @Size(max = 255, message = "Contact info must be less than 255 characters")
            String contactInfo
    ) {
    }

    public record GoogleLoginRequest(
            @NotBlank(message = "ID Token is required")
            String idToken
    ) {
    }

    public record LoginRequest(
            @NotBlank(message = "Email is required")
            @Email(message = "Invalid email format") String email,

            @NotBlank(message = "Password is required") String password
    ) {
    }

    public record UpdateProfileRequest(
            @NotBlank(message = "Display name is required")
            @Size(max = 100) String displayName,

            @Size(max = 255, message = "Contact info must be less than 255 characters")
            String contactInfo
    ) {
    }

    public record ChangePasswordRequest(
            @NotBlank(message = "Current password is required") String oldPassword,

            @NotBlank(message = "New password is required")
            @Size(min = 8, max = 128)
            @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
                    message = "New password does not meet complexity requirements")
            String newPassword
    ) {
    }

    public record TokenRefreshRequest(
            @NotBlank(message = "Refresh token is required")
            String refreshToken
    ) {
    }

    public record ConfirmPasswordResetRequest(
            @NotBlank(message = "Reset token is required")
            String token,

            @NotBlank(message = "New password is required")
            @Size(min = 8, max = 128)
            @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
                    message = "New password does not meet complexity requirements")
            String newPassword
    ) {
    }
}