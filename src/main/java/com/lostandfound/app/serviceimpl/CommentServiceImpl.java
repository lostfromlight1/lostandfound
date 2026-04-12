package com.lostandfound.app.serviceimpl;

import com.lostandfound.app.dto.request.CommentRequest;
import com.lostandfound.app.dto.response.CommentResponse;
import com.lostandfound.app.model.Comment;
import com.lostandfound.app.model.Post;
import com.lostandfound.app.model.User;
import com.lostandfound.app.repository.CommentRepository;
import com.lostandfound.app.repository.PostRepository;
import com.lostandfound.app.repository.UserRepository;
import com.lostandfound.app.security.CustomUserDetails;
import com.lostandfound.app.service.CommentService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    public CommentResponse createComment(CommentRequest.CreateComment request, CustomUserDetails currentUser) {
        Post post = postRepository.findById(request.postId())
                .orElseThrow(() -> new RuntimeException("Post not found"));

        User user = userRepository.getReferenceById(currentUser.getId());

        Comment comment = Comment.builder()
                .content(request.content())
                .post(post)
                .user(user)
                .build();

        return CommentResponse.fromEntity(commentRepository.save(comment));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPost(Long postId) {
        return commentRepository.findByPostIdAndActiveTrue(postId).stream()
                .map(CommentResponse::fromEntity)
                .toList();
    }

    @Override
    public CommentResponse updateComment(Long commentId, CommentRequest.UpdateComment request, CustomUserDetails currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));


        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only update your own comments");
        }

        comment.setContent(request.content());
        return CommentResponse.fromEntity(comment);
    }

    @Override
    public void deleteComment(Long commentId, CustomUserDetails currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only delete your own comments");
        }

        comment.softDelete();
    }
}
