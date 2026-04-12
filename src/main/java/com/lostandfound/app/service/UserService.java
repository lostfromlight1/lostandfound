package com.lostandfound.app.service;

import com.lostandfound.app.dto.request.AuthRequest.UpdateProfileRequest;
import com.lostandfound.app.dto.response.UserResponse;
import com.lostandfound.app.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
// import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    // --- Current User Actions ---
    UserResponse getMe(User currentUser);

    UserResponse updateProfile(User currentUser, UpdateProfileRequest request);

    /*
     * TODO: Uncomment when Image entity and AWS S3/Local file storage is configured
     * UserResponse uploadProfilePicture(User currentUser, MultipartFile file);
     */

    // --- Public / Admin Actions ---
    UserResponse getPublicProfile(Long userId);

    Page<UserResponse> getAllUsers(Pageable pageable);

    Page<UserResponse> searchUsers(String query, Pageable pageable);

    /**
     * Locks a user account, preventing them from logging in or making requests.
     */
    void banUser(Long userId);

    void unbanUser(Long userId);
}