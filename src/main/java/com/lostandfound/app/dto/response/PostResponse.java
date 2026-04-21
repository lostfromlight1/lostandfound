package com.lostandfound.app.dto.response;

import com.lostandfound.app.model.MyanmarCity;
import com.lostandfound.app.model.PostStatus;
import com.lostandfound.app.model.PostType;

import java.time.LocalDate;
import java.util.List;

public class PostResponse {

    public record PostDto(
            Long id,
            String title,
            String description,
            PostType type,
            PostStatus status,
            MyanmarCity city,
            String locationDetails,
            Double latitude,
            Double longitude,
            LocalDate lostFoundDate,
            String contactInfo,
            java.math.BigDecimal reward,

            UserSummary user,
            CategoryDto category,

            List<ImageDto> images
    ) {}

    public record UserSummary(
            Long id,
            String displayName,
            String avatarUrl,
            String email
    ) {}

    public record CategoryDto(
            Long id,
            String name
    ) {}

    public record ImageDto(
            Long id,
            String url,
            Integer sortOrder
    ) {}
}