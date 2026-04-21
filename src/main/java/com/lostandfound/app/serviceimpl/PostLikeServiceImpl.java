package com.lostandfound.app.serviceimpl;

import com.lostandfound.app.model.Post;
import com.lostandfound.app.model.PostLike;
import com.lostandfound.app.model.User;
import com.lostandfound.app.repository.PostLikeRepository;
import com.lostandfound.app.repository.PostRepository;
import com.lostandfound.app.repository.UserRepository;
import com.lostandfound.app.security.CustomUserDetails;
import com.lostandfound.app.service.PostLikeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostLikeServiceImpl implements PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

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
            Post post = postRepository.getReferenceById(postId);
            User user = userRepository.getReferenceById(userId);

            PostLike like = new PostLike();
            like.setPost(post);
            like.setUser(user);

            postLikeRepository.save(like);
        }
    }
}
