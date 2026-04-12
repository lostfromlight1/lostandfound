package com.lostandfound.app.config;

import com.lostandfound.app.repository.CommentRepository;
import com.lostandfound.app.repository.PostRepository;
import com.lostandfound.app.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component("authConfig")
public class AuthorizationConfig {

  private final PostRepository postRepository;
  private final CommentRepository commentRepository;

  public boolean isPostOwner(Long postId) {
    Long currentUserId = getCurrentUserId();
    if (currentUserId == null) {
      log.warn("Post ownership check failed: No authenticated user found.");
      return false;
    }

    return postRepository.findById(postId)
            .map(post -> post.getUser().getId().equals(currentUserId))
            .orElse(false);
  }

  public boolean isCommentOwner(Long commentId) {
    Long currentUserId = getCurrentUserId();
    if (currentUserId == null) {
      log.warn("Comment ownership check failed: No authenticated user found.");
      return false;
    }

    return commentRepository.findById(commentId)
            .map(comment -> comment.getUser().getId().equals(currentUserId))
            .orElse(false);
  }

  private Long getCurrentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
      return userDetails.getId();
    }
    return null;
  }
}