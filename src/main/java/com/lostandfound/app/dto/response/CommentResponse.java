package com.lostandfound.app.dto.response;

import com.lostandfound.app.model.Comment;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CommentResponse(
        Long id,
        String content,
        Long userId,
        String authorName,
        String authorAvatarUrl,
        Long postId,
        String imageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CommentResponse fromEntity(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .userId(comment.getUser().getId())
                .authorName(comment.getUser().getDisplayName())
                .authorAvatarUrl(comment.getUser().getAvatarUrl())
                .postId(comment.getPost().getId())
                .imageUrl(comment.getImageUrl())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}