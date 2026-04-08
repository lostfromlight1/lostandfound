package com.lostandfound.app.service;

import com.lostandfound.app.dto.request.AuthRequest;
import com.lostandfound.app.dto.request.AuthRequest.ChangePasswordRequest;
import com.lostandfound.app.dto.request.AuthRequest.LoginRequest;
import com.lostandfound.app.dto.request.AuthRequest.RegisterRequest;
import com.lostandfound.app.dto.response.AuthResponse;
import com.lostandfound.app.dto.response.UserResponse;
import com.lostandfound.app.model.User;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void changePassword(User currentUser, ChangePasswordRequest request);

    void resetPassword(String email);

    AuthResponse refreshToken(AuthRequest.TokenRefreshRequest request);

    void confirmPasswordReset(String token, String newPassword);
}