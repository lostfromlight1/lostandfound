package com.lostandfound.app.config;

import com.lostandfound.app.model.User;
// import com.lostandfound.app.repository.CommentRepository;
// import com.lostandfound.app.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component("authConfig")
public class AuthorizationConfig {

//  private final PostRepository postRepository;
//  private final CommentRepository commentRepository;

  public boolean isPostOwner(Long postId) {
    Long currentUserId = getCurrentUserId();
    if (currentUserId == null) return false;

//    return postRepository.findById(postId)
//            .map(post -> post.getUser().getId().equals(currentUserId))
//            .orElse(false);

    // TODO: Implement actual database check once PostRepository is created
    log.warn("Post ownership check is stubbed. Returning false.");
    return false; // TEMPORARY: Change to 'true' if you need to bypass this check for testing right now.
  }

  public boolean isCommentOwner(Long commentId) {
    Long currentUserId = getCurrentUserId();
    if (currentUserId == null) return false;

//    return commentRepository.findById(commentId)
//            .map(comment -> comment.getUser().getId().equals(currentUserId))
//            .orElse(false);

    // TODO: Implement actual database check once CommentRepository is created
    log.warn("Comment ownership check is stubbed. Returning false.");
    return false; // TEMPORARY: Change to 'true' if you need to bypass this check for testing right now.
  }

  private Long getCurrentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User user) {
      return user.getId();
    }
    return null;
  }
}