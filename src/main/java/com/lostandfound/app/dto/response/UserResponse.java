package com.lostandfound.app.dto.response;

import com.lostandfound.app.model.Role;
import lombok.Builder;

@Builder
public record UserResponse(
        Long id,
        String email,
        String displayName,
        String contactInfo,
        Role role,
        String avatarUrl
) {}