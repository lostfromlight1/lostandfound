package com.lostandfound.app.controller;

import com.lostandfound.app.annotation.ApiId;
import com.lostandfound.app.dto.request.AuthRequest.UpdateProfileRequest;
import com.lostandfound.app.dto.response.BaseResponse;
import com.lostandfound.app.dto.response.UserResponse;
import com.lostandfound.app.model.User;
import com.lostandfound.app.annotation.CheckSecurity;
import com.lostandfound.app.annotation.CurrentUser;
import com.lostandfound.app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "2. User Management", description = "Endpoints for user profiles, searches, and administrative actions")
public class UserController {

    private final UserService userService;

    // -------------------------------------------------------------------------
    // Current User Endpoints
    // -------------------------------------------------------------------------

    @GetMapping("/me")
    @ApiId("USR-001")
    @Operation(summary = "Get My Profile", description = "Fetches the profile of the currently authenticated user.")
    public ResponseEntity<BaseResponse<UserResponse>> getMe(
            @Parameter(hidden = true) @CurrentUser User currentUser) {
        log.info("REST request to get profile for current user ID: {}", currentUser.getId());
        UserResponse response = userService.getMe(currentUser);
        return BaseResponse.success("Profile fetched successfully", response);
    }

    @PutMapping("/update")
    @ApiId("USR-002")
    @Operation(summary = "Update My Profile", description = "Updates the display name and contact info of the current user.")
    public ResponseEntity<BaseResponse<UserResponse>> updateProfile(
            @Parameter(hidden = true) @CurrentUser User currentUser,
            @Valid @RequestBody UpdateProfileRequest request) {
        log.info("REST request to update profile for user ID: {}", currentUser.getId());
        UserResponse response = userService.updateProfile(currentUser, request);
        return BaseResponse.success("Profile updated successfully", response);
    }

    // -------------------------------------------------------------------------
    // Public Endpoints
    // -------------------------------------------------------------------------

    @GetMapping("/{id}/profile")
    @CheckSecurity.Public.canRead
    @ApiId("USR-003")
    @Operation(summary = "Get Public Profile", description = "Fetches the public-facing profile of any user by their ID.")
    public ResponseEntity<BaseResponse<UserResponse>> getPublicProfile(
            @PathVariable Long id) {
        log.info("REST request to get public profile for user ID: {}", id);
        UserResponse response = userService.getPublicProfile(id);
        return BaseResponse.success("Public profile fetched successfully", response);
    }

    // -------------------------------------------------------------------------
    // Administrator Endpoints
    // -------------------------------------------------------------------------

    @GetMapping
    @CheckSecurity.Admin.isRequired
    @ApiId("USR-004")
    @Operation(summary = "Get All Users (Admin)", description = "Returns a paginated list of all registered users.")
    public ResponseEntity<BaseResponse<Page<UserResponse>>> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("REST request to get all users. Page: {}", pageable.getPageNumber());
        Page<UserResponse> response = userService.getAllUsers(pageable);
        return BaseResponse.success("Users fetched successfully", response);
    }

    @GetMapping("/search")
    @CheckSecurity.Admin.isRequired
    @ApiId("USR-005")
    @Operation(summary = "Search Users (Admin)", description = "Searches users by email or display name.")
    public ResponseEntity<BaseResponse<Page<UserResponse>>> searchUsers(
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("REST request to search users with query: '{}'", query);
        Page<UserResponse> response = userService.searchUsers(query, pageable);
        return BaseResponse.success("User search completed", response);
    }

    @PutMapping("/{id}/ban")
    @CheckSecurity.Admin.isRequired
    @ApiId("USR-006")
    @Operation(summary = "Ban User (Admin)", description = "Locks a user account, preventing future logins.")
    public ResponseEntity<BaseResponse<Void>> banUser(
            @PathVariable Long id) {
        log.info("REST request to ban user ID: {}", id);
        userService.banUser(id);
        return BaseResponse.success("User has been banned successfully");
    }

    @PutMapping("/{id}/unban")
    @CheckSecurity.Admin.isRequired
    @ApiId("USR-007")
    @Operation(summary = "Unban User (Admin)", description = "Unlocks a previously banned user account, allowing them to log in again.")
    public ResponseEntity<BaseResponse<Void>> unbanUser(
            @PathVariable Long id) {

        log.info("REST request to unban user ID: {}", id);
        userService.unbanUser(id);
        return BaseResponse.success("User has been unbanned successfully");
    }
}