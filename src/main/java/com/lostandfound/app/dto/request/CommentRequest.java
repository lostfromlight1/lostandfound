package com.lostandfound.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CommentRequest {

    public record CreateComment(
            @NotNull(message = "Post ID is required")
            Long postId,

            @NotBlank(message = "Comment content cannot be empty")
            @Size(max = 1000, message = "Comment must be under 1000 characters")
            String content
    ) {}

    public record UpdateComment(
            @NotBlank(message = "Comment content cannot be empty")
            @Size(max = 1000, message = "Comment must be under 1000 characters")
            String content
    ) {}
}
