package com.lostandfound.app.serviceimpl;

import com.lostandfound.app.dto.request.CommentRequest;
import com.lostandfound.app.dto.response.CommentResponse;
import com.lostandfound.app.dto.response.ReplyResponse;
import com.lostandfound.app.exception.AppException;
import com.lostandfound.app.exception.ErrorCode;
import com.lostandfound.app.model.Comment;
import com.lostandfound.app.model.Post;
import com.lostandfound.app.model.Reply;
import com.lostandfound.app.model.User;
import com.lostandfound.app.repository.CommentRepository;
import com.lostandfound.app.repository.PostRepository;
import com.lostandfound.app.repository.ReplyRepository;
import com.lostandfound.app.repository.UserRepository;
import com.lostandfound.app.security.CustomUserDetails;
import com.lostandfound.app.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ReplyRepository replyRepository;

    @Override
    public CommentResponse createComment(CommentRequest.CreateComment request, CustomUserDetails currentUser) {
        log.info("[{}] Creating comment for post ID: {} by user ID: {}", getTraceId(), request.postId(), currentUser.getId());

        Post post = postRepository.findById(request.postId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Post not found with id: " + request.postId()));

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

        return commentRepository.findActiveCommentsWithUserByPostId(postId)
                .stream()
                .map(comment -> {
                    // Fetch replies for each comment
                    List<Reply> rootReplies = replyRepository.findActiveRootRepliesByCommentId(comment.getId());
                    List<ReplyResponse> replyResponses = rootReplies.stream()
                            .map(reply -> {
                                List<Reply> nestedReplies = replyRepository.findActiveNestedRepliesByReplyId(reply.getId());
                                List<ReplyResponse> nestedResponses = nestedReplies.stream()
                                        .map(ReplyResponse::fromEntity)
                                        .toList();
                                return ReplyResponse.fromEntityWithNestedReplies(reply, nestedResponses);
                            })
                            .toList();

                    return CommentResponse.fromEntityWithReplies(comment, replyResponses);
                })
                .toList();
    }

    @Override
    public CommentResponse updateComment(Long commentId, CommentRequest.UpdateComment request, CustomUserDetails currentUser) {
        log.info("[{}] Updating comment ID: {} by user ID: {}", getTraceId(), commentId, currentUser.getId());

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Comment not found with id: " + commentId));

        if (!comment.getUser().getId().equals(currentUser.getId())) {
            log.warn("[{}] User ID: {} attempted to update comment ID: {} owned by user ID: {}",
                    getTraceId(), currentUser.getId(), commentId, comment.getUser().getId());
            throw new AccessDeniedException("You can only update your own comments");
        }

        comment.setContent(request.content());
        comment.setImageUrl(request.imageUrl());
        comment.setImagePublicId(request.imagePublicId());

        log.info("[{}] Comment ID: {} updated successfully", getTraceId(), commentId);
        return CommentResponse.fromEntity(comment);
    }

    @Override
    public void deleteComment(Long commentId, CustomUserDetails currentUser) {
        log.info("[{}] Deleting comment ID: {} by user ID: {}", getTraceId(), commentId, currentUser.getId());

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Comment not found with id: " + commentId));

        boolean isAuthor = comment.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAuthor && !isAdmin) {
            log.warn("[{}] User ID: {} (Admin: {}) attempted to delete comment ID: {} owned by user ID: {}",
                    getTraceId(), currentUser.getId(), isAdmin, commentId, comment.getUser().getId());
            throw new AccessDeniedException("You can only delete your own comments");
        }

        comment.softDelete();
        log.info("[{}] Comment ID: {} soft deleted successfully by user ID: {}", getTraceId(), commentId, currentUser.getId());
    }

    private String getTraceId() {
        return Objects.toString(MDC.get("traceId"), "SYSTEM");
    }
}
