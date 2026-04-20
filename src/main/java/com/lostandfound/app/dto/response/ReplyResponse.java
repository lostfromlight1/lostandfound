package com.lostandfound.app.dto.response;

import com.lostandfound.app.model.Reply;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ReplyResponse(
        Long id,
        String content,
        Long userId,
        String authorName,
        String authorAvatarUrl,
        Long commentId,
        Long replyToId,
        String replyToAuthorName,
        String imageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ReplyResponse> nestedReplies
) {
    public static ReplyResponse fromEntity(Reply reply) {
        return ReplyResponse.builder()
                .id(reply.getId())
                .content(reply.getContent())
                .userId(reply.getUser().getId())
                .authorName(reply.getUser().getDisplayName())
                .authorAvatarUrl(reply.getUser().getAvatarUrl())
                .commentId(reply.getComment().getId())
                .replyToId(reply.getReplyTo() != null ? reply.getReplyTo().getId() : null)
                .replyToAuthorName(reply.getReplyTo() != null ? reply.getReplyTo().getUser().getDisplayName() : null)
                .imageUrl(reply.getImageUrl())
                .createdAt(reply.getCreatedAt())
                .updatedAt(reply.getUpdatedAt())
                .build();
    }

    public static ReplyResponse fromEntityWithNestedReplies(Reply reply, List<ReplyResponse> nestedReplies) {
        return ReplyResponse.builder()
                .id(reply.getId())
                .content(reply.getContent())
                .userId(reply.getUser().getId())
                .authorName(reply.getUser().getDisplayName())
                .authorAvatarUrl(reply.getUser().getAvatarUrl())
                .commentId(reply.getComment().getId())
                .replyToId(reply.getReplyTo() != null ? reply.getReplyTo().getId() : null)
                .replyToAuthorName(reply.getReplyTo() != null ? reply.getReplyTo().getUser().getDisplayName() : null)
                .imageUrl(reply.getImageUrl())
                .createdAt(reply.getCreatedAt())
                .updatedAt(reply.getUpdatedAt())
                .nestedReplies(nestedReplies)
                .build();
    }
}
