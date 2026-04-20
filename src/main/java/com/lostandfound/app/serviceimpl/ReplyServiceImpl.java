package com.lostandfound.app.serviceimpl;

import com.lostandfound.app.dto.request.ReplyRequest;
import com.lostandfound.app.dto.response.ReplyResponse;
import com.lostandfound.app.exception.AppException;
import com.lostandfound.app.exception.ErrorCode;
import com.lostandfound.app.model.Comment;
import com.lostandfound.app.model.Reply;
import com.lostandfound.app.model.User;
import com.lostandfound.app.repository.CommentRepository;
import com.lostandfound.app.repository.ReplyRepository;
import com.lostandfound.app.repository.UserRepository;
import com.lostandfound.app.security.CustomUserDetails;
import com.lostandfound.app.service.ReplyService;
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
public class ReplyServiceImpl implements ReplyService {

    private final ReplyRepository replyRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Override
    public ReplyResponse createReply(ReplyRequest.CreateReply request, CustomUserDetails currentUser) {
        log.info("[{}] Creating reply for comment ID: {} by user ID: {}", getTraceId(), request.commentId(), currentUser.getId());

        Comment comment = commentRepository.findById(request.commentId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Comment not found with id: " + request.commentId()));

        User user = userRepository.getReferenceById(currentUser.getId());

        Reply replyTo = null;
        if (request.replyToId() != null) {
            replyTo = replyRepository.findById(request.replyToId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                            "Reply not found with id: " + request.replyToId()));
        }

        try {
            Reply reply = Reply.builder()
                    .content(request.content())
                    .comment(comment)
                    .user(user)
                    .replyTo(replyTo)
                    .imageUrl(request.imageUrl())
                    .imagePublicId(request.imagePublicId())
                    .build();

            Reply savedReply = replyRepository.save(reply);
            log.info("[{}] Reply ID: {} created successfully for comment ID: {}", getTraceId(), savedReply.getId(), request.commentId());

            return ReplyResponse.fromEntity(savedReply);
        } catch (Exception e) {
            log.error("[{}] Error creating reply: {}", getTraceId(), e.getMessage());
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to create reply");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReplyResponse> getRepliesByComment(Long commentId) {
        log.info("[{}] Fetching all replies for comment ID: {}", getTraceId(), commentId);

        // Verify comment exists
        commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Comment not found with id: " + commentId));

        List<Reply> rootReplies = replyRepository.findActiveRootRepliesByCommentId(commentId);

        return rootReplies.stream()
                .map(reply -> {
                    List<Reply> nestedReplies = replyRepository.findActiveNestedRepliesByReplyId(reply.getId());
                    List<ReplyResponse> nestedResponses = nestedReplies.stream()
                            .map(ReplyResponse::fromEntity)
                            .toList();
                    return ReplyResponse.fromEntityWithNestedReplies(reply, nestedResponses);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReplyResponse> getRootRepliesByComment(Long commentId) {
        log.info("[{}] Fetching root level replies for comment ID: {}", getTraceId(), commentId);

        return replyRepository.findActiveRootRepliesByCommentId(commentId)
                .stream()
                .map(ReplyResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReplyResponse> getNestedReplies(Long replyId) {
        log.info("[{}] Fetching nested replies for reply ID: {}", getTraceId(), replyId);

        // Verify reply exists
        replyRepository.findById(replyId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Reply not found with id: " + replyId));

        return replyRepository.findActiveNestedRepliesByReplyId(replyId)
                .stream()
                .map(ReplyResponse::fromEntity)
                .toList();
    }

    @Override
    public ReplyResponse updateReply(Long replyId, ReplyRequest.UpdateReply request, CustomUserDetails currentUser) {
        log.info("[{}] Updating reply ID: {} by user ID: {}", getTraceId(), replyId, currentUser.getId());

        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Reply not found with id: " + replyId));

        if (!reply.getUser().getId().equals(currentUser.getId())) {
            log.warn("[{}] User ID: {} attempted to update reply ID: {} owned by user ID: {}",
                    getTraceId(), currentUser.getId(), replyId, reply.getUser().getId());
            throw new AccessDeniedException("You can only update your own replies");
        }

        reply.setContent(request.content());
        reply.setImageUrl(request.imageUrl());
        reply.setImagePublicId(request.imagePublicId());

        log.info("[{}] Reply ID: {} updated successfully", getTraceId(), replyId);
        return ReplyResponse.fromEntity(reply);
    }

    @Override
    public void deleteReply(Long replyId, CustomUserDetails currentUser) {
        log.info("[{}] Deleting reply ID: {} by user ID: {}", getTraceId(), replyId, currentUser.getId());

        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Reply not found with id: " + replyId));

        boolean isAuthor = reply.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAuthor && !isAdmin) {
            log.warn("[{}] User ID: {} (Admin: {}) attempted to delete reply ID: {} owned by user ID: {}",
                    getTraceId(), currentUser.getId(), isAdmin, replyId, reply.getUser().getId());
            throw new AccessDeniedException("You can only delete your own replies");
        }

        reply.softDelete();
        log.info("[{}] Reply ID: {} soft deleted successfully by user ID: {}", getTraceId(), replyId, currentUser.getId());
    }

    private String getTraceId() {
        return Objects.toString(MDC.get("traceId"), "SYSTEM");
    }
}
