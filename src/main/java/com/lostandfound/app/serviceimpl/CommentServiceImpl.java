package com.lostandfound.app.serviceimpl;

import com.lostandfound.app.dto.request.CommentRequest;
import com.lostandfound.app.dto.response.CommentResponse;
import com.lostandfound.app.exception.AppException;
import com.lostandfound.app.exception.ErrorCode;
import com.lostandfound.app.model.Comment;
import com.lostandfound.app.model.Post;
import com.lostandfound.app.model.User;
import com.lostandfound.app.repository.CommentRepository;
import com.lostandfound.app.repository.PostRepository;
import com.lostandfound.app.repository.UserRepository;
import com.lostandfound.app.security.CustomUserDetails;
import com.lostandfound.app.service.CommentService;
import org.slf4j.MDC;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    public CommentResponse createComment(CommentRequest.CreateComment request, CustomUserDetails currentUser) {
        log.info("[{}] Attempting to create comment on post ID: {}", getTraceId(), request.postId());

        Post post = postRepository.findById(request.postId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Post not found"));

        User user = userRepository.getReferenceById(currentUser.getId());

        try {
            Comment comment = Comment.builder()
                    .content(request.content())
                    .post(post)
                    .user(user)
                    .imageUrl(request.imageUrl())
                    .imagePublicId(request.imagePublicId())
                    .build();

            Comment savedComment = commentRepository.save(comment);
            log.info("[{}] Comment ID: {} created successfully", getTraceId(), savedComment.getId());

            return CommentResponse.fromEntity(savedComment);
        } catch (Exception e) {
            log.error("[{}] Error creating comment: {}", getTraceId(), e.getMessage());
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to create comment");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPost(Long postId) {
        log.info("[{}] Fetching comments for post ID: {}", getTraceId(), postId);
        return commentRepository.findByPostIdAndActiveTrue(postId).stream()
                .map(CommentResponse::fromEntity)
                .toList();
    }

    @Override
    public CommentResponse updateComment(Long commentId, CommentRequest.UpdateComment request, CustomUserDetails currentUser) {
        log.info("[{}] Attempting to update comment ID: {}", getTraceId(), commentId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Comment not found"));

        if (!comment.getUser().getId().equals(currentUser.getId())) {
            log.warn("[{}] User ID: {} attempted to modify comment ID: {} without permission", getTraceId(), currentUser.getId(), commentId);
            throw new AppException(ErrorCode.POST_MODIFICATION_DENIED, "You can only update your own comments");
        }

        comment.setContent(request.content());
        comment.setImageUrl(request.imageUrl());
        comment.setImagePublicId(request.imagePublicId());

        log.info("[{}] Comment ID: {} updated successfully", getTraceId(), comment.getId());
        return CommentResponse.fromEntity(comment);
    }

    @Override
    public void deleteComment(Long commentId, CustomUserDetails currentUser) {
        log.info("[{}] Attempting to delete comment ID: {}", getTraceId(), commentId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Comment not found"));

        if (!comment.getUser().getId().equals(currentUser.getId())) {
            log.warn("[{}] User ID: {} attempted to delete comment ID: {} without permission", getTraceId(), currentUser.getId(), commentId);
            throw new AppException(ErrorCode.POST_MODIFICATION_DENIED, "You can only delete your own comments");
        }

        comment.softDelete();
        log.info("[{}] Comment ID: {} soft deleted successfully", getTraceId(), commentId);
    }

    private String getTraceId() {
        return Objects.toString(MDC.get("traceId"), "SYSTEM");
    }
}