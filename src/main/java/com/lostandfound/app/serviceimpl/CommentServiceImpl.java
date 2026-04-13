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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    public CommentResponse createComment(CommentRequest.CreateComment request, CustomUserDetails currentUser) {
        log.debug("Creating comment for post ID: {} by user ID: {}", request.postId(), currentUser.getId());

        Post post = postRepository.findById(request.postId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Post not found with id: " + request.postId()));

        User user = userRepository.getReferenceById(currentUser.getId());

        Comment comment = Comment.builder()
                .content(request.content())
                .post(post)
                .user(user)
                .build();

        Comment savedComment = commentRepository.save(comment);
        log.info("Comment ID: {} created successfully", savedComment.getId());

        return CommentResponse.fromEntity(savedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPost(Long postId) {
        log.debug("Fetching comments for post ID: {}", postId);

        return commentRepository.findActiveCommentsWithUserByPostId(postId)
                .stream()
                .map(CommentResponse::fromEntity)
                .toList();
    }

    @Override
    public CommentResponse updateComment(Long commentId, CommentRequest.UpdateComment request, CustomUserDetails currentUser) {
        log.debug("Updating comment ID: {} by user ID: {}", commentId, currentUser.getId());

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Comment not found with id: " + commentId));

        if (!comment.getUser().getId().equals(currentUser.getId())) {
            log.warn("User ID: {} attempted to update comment ID: {} owned by user ID: {}",
                    currentUser.getId(), commentId, comment.getUser().getId());
            throw new AccessDeniedException("You can only update your own comments");
        }

        comment.setContent(request.content());
        log.info("Comment ID: {} updated successfully", commentId);

        return CommentResponse.fromEntity(comment);
    }

    @Override
    public void deleteComment(Long commentId, CustomUserDetails currentUser) {
        log.debug("Deleting comment ID: {} by user ID: {}", commentId, currentUser.getId());

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Comment not found with id: " + commentId));

        boolean isAuthor = comment.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAuthor && !isAdmin) {
            log.warn("User ID: {} (Admin: {}) attempted to delete comment ID: {} owned by user ID: {}",
                    currentUser.getId(), isAdmin, commentId, comment.getUser().getId());
            throw new AccessDeniedException("You can only delete your own comments");
        }

        comment.softDelete();
        log.info("Comment ID: {} soft deleted successfully by user ID: {}", commentId, currentUser.getId());
    }
}
