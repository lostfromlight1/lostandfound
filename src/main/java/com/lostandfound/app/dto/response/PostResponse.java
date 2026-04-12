package com.lostandfound.app.dto.response;

import com.lostandfound.app.model.PostStatus;
import com.lostandfound.app.model.PostType;

import java.time.LocalDate;

public class PostResponse {

    public record PostDto(
            Long id,

            String title,

            String description,

            PostType type,

            PostStatus status,

            String location,

            LocalDate lostFoundDate,

            String contactInfo,

            java.math.BigDecimal reward,


            // nested objects
            UserSummary user,
            CategoryDto category
    ) {}



    public record UserSummary(
            Long id,
            String displayName
    ) {}



    public record CategoryDto(
            Long id,
            String name
    ) {}


}