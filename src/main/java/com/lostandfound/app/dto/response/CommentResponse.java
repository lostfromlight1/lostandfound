package com.lostandfound.app.dto.response;

import com.lostandfound.app.model.Comment;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        String content,
        Long userId,
        String username,
        Long postId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CommentResponse fromEntity(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getUser().getId(),
                comment.getUser().getUsername(),
                comment.getPost().getId(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}