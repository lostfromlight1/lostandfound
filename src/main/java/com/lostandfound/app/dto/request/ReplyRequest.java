package com.lostandfound.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ReplyRequest {

    public record CreateReply(
            @NotNull(message = "Comment ID is required")
            Long commentId,

            @NotBlank(message = "Reply content cannot be empty")
            @Size(max = 1000, message = "Reply must be under 1000 characters")
            String content,

            String imageUrl,

            String imagePublicId,

            Long replyToId
    ) {}

    public record UpdateReply(
            @NotBlank(message = "Reply content cannot be empty")
            @Size(max = 1000, message = "Reply must be under 1000 characters")
            String content,

            String imageUrl,

            String imagePublicId
    ) {}
}
