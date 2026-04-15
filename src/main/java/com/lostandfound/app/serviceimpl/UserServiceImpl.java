package com.lostandfound.app.serviceimpl;

import com.lostandfound.app.dto.request.AuthRequest.UpdateProfileRequest;
import com.lostandfound.app.dto.response.PageResponse;
import com.lostandfound.app.dto.response.UserResponse;
import com.lostandfound.app.exception.AppException;
import com.lostandfound.app.exception.ErrorCode;
import com.lostandfound.app.model.User;
import com.lostandfound.app.repository.UserRepository;
import com.lostandfound.app.service.BaseService;
import com.lostandfound.app.service.RefreshTokenService;
import com.lostandfound.app.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lostandfound.app.service.ImageService;
import com.lostandfound.app.dto.response.ImageUploadResponse;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends BaseService implements UserService {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final ImageService imageService;

    @Override
    public UserResponse getMe(User currentUser) {
        validateCurrentUser(currentUser);
        log.info("[{}] Fetching profile for current user ID: {}", getTraceId(), currentUser.getId());
        return mapToResponse(fetchUserById(currentUser.getId()));
    }

    @Override
    @Transactional
    public UserResponse updateProfile(User currentUser, UpdateProfileRequest request) {
        validateCurrentUser(currentUser);
        log.info("[{}] Updating profile for user ID: {}", getTraceId(), currentUser.getId());

        User user = fetchUserById(currentUser.getId());
        user.setDisplayName(request.displayName());
        user.setContactInfo(request.contactInfo());

        user.setAvatarUrl(request.avatarUrl());
        user.setAvatarPublicId(request.avatarPublicId());

        User updated = userRepository.save(user);
        log.info("[{}] Profile updated successfully for user ID: {}", getTraceId(), updated.getId());

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public UserResponse uploadProfilePicture(User currentUser, MultipartFile file) {
        validateCurrentUser(currentUser);
        log.info("[{}] Uploading new profile picture for user ID: {}", getTraceId(), currentUser.getId());

        User user = fetchUserById(currentUser.getId());

        if (user.getAvatarPublicId() != null && !user.getAvatarPublicId().isEmpty()) {
            imageService.deleteImage(user.getAvatarPublicId());
        }

        ImageUploadResponse uploadedImage = imageService.uploadImage(file, "avatars");

        user.setAvatarUrl(uploadedImage.getUrl());
        user.setAvatarPublicId(uploadedImage.getPublicId());

        User updatedUser = userRepository.save(user);

        return mapToResponse(updatedUser);
    }

    @Override
    public UserResponse getPublicProfile(Long userId) {
        log.info("[{}] Fetching public profile for user ID: {}", getTraceId(), userId);
        return mapToResponse(fetchUserById(userId));
    }

    @Override
    public PageResponse<UserResponse> getAllUsers(Pageable pageable) {
        log.info("[{}] Fetching all users, page={}", getTraceId(), pageable.getPageNumber());
        Page<UserResponse> page = userRepository.findAll(pageable).map(this::mapToResponse);

        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    // ⚠️ FIXED: Returning PageResponse mapping
    @Override
    public PageResponse<UserResponse> searchUsers(String query, Pageable pageable) {
        log.info("[{}] Searching users with query='{}', page={}", getTraceId(), query, pageable.getPageNumber());
        Page<User> users = userRepository.searchUsers(query, pageable);
        log.info("[{}] Search returned {} total users", getTraceId(), users.getTotalElements());

        Page<UserResponse> page = users.map(this::mapToResponse);

        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Override
    @Transactional
    public void banUser(Long userId) {
        log.info("[{}] Attempting to ban user ID: {}", getTraceId(), userId);

        User user = fetchUserById(userId);

        if (Boolean.TRUE.equals(user.getIsLocked())) {
            log.info("[{}] User ID: {} is already banned", getTraceId(), userId);
            return;
        }

        user.setIsLocked(true);
        userRepository.save(user);

        refreshTokenService.revokeByUser(userId);

        log.warn("[{}] User ID: {} has been locked/banned and all refresh tokens revoked", getTraceId(), userId);
    }

    @Override
    @Transactional
    public void unbanUser(Long userId) {
        log.info("[{}] Attempting to unban user ID: {}", getTraceId(), userId);

        User user = fetchUserById(userId);

        if (Boolean.FALSE.equals(user.getIsLocked())) {
            log.info("[{}] User ID: {} is already active and unbanned", getTraceId(), userId);
            return;
        }

        user.setIsLocked(false);
        userRepository.save(user);

        log.info("[{}] User ID: {} has been unlocked/unbanned successfully", getTraceId(), userId);
    }

    // ------------------ Helpers ------------------

    private void validateCurrentUser(User currentUser) {
        if (currentUser == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Access Denied: Missing or invalid authentication token.");
        }
    }

    private User fetchUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[{}] User not found: {}", getTraceId(), userId);
                    return new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User profile not found");
                });
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .contactInfo(user.getContactInfo())
                .role(user.getRole())
                .avatarUrl(user.getAvatarUrl())
                .isLocked(user.getIsLocked())
                .build();
    }
}