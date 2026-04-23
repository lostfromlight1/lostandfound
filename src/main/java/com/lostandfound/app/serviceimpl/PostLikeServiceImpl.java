package com.lostandfound.app.serviceimpl;

import com.lostandfound.app.exception.AppException;
import com.lostandfound.app.exception.ErrorCode;
import com.lostandfound.app.model.Post;
import com.lostandfound.app.model.PostLike;
import com.lostandfound.app.model.User;
import com.lostandfound.app.repository.PostLikeRepository;
import com.lostandfound.app.repository.PostRepository;
import com.lostandfound.app.repository.UserRepository;
import com.lostandfound.app.security.CustomUserDetails;
import com.lostandfound.app.service.NotificationService;
import com.lostandfound.app.service.PostLikeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostLikeServiceImpl implements PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    @Override
    public void toggleLike(Long postId, CustomUserDetails customUserDetails) {
        Long userId = customUserDetails.getId();

        if (postLikeRepository.existsByUserIdAndPostId(userId, postId)) {
            // ❌ Unlike
            postLikeRepository.deleteByUserIdAndPostId(userId, postId);
        } else {
            // ✅ Like
            // Use getReferenceById to avoid unnecessary SELECT queries
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Post not found"));
            User user = userRepository.getReferenceById(userId);

            PostLike like = new PostLike();
            like.setPost(post);
            like.setUser(user);

            postLikeRepository.save(like);

            if (!post.getUser().getId().equals(userId)) {
                notificationService.notifyPostLiked(
                        postId,
                        userId,
                        customUserDetails.getUsername()
                );
                log.info("Notification sent to post owner for like on post ID: {}", postId);
            } else {
                log.info("No notification sent - user liked their own post");
            }

        }
    }
}
