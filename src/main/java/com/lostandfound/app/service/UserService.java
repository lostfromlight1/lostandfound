package com.lostandfound.app.service;

import com.lostandfound.app.dto.request.AuthRequest.UpdateProfileRequest;
import com.lostandfound.app.dto.response.PageResponse;
import com.lostandfound.app.dto.response.UserResponse;
import com.lostandfound.app.security.CustomUserDetails;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    UserResponse getMe(CustomUserDetails currentUser);

    UserResponse updateProfile(CustomUserDetails currentUser, UpdateProfileRequest request);

    UserResponse uploadProfilePicture(CustomUserDetails currentUser, MultipartFile file);

    // --- Public / Admin Actions ---
    UserResponse getPublicProfile(Long userId);

    PageResponse<UserResponse> getAllUsers(Pageable pageable);

    PageResponse<UserResponse> searchUsers(String query, Pageable pageable);

    void banUser(Long userId);

    void unbanUser(Long userId);
}