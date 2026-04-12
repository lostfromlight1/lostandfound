package com.lostandfound.app.service;

import com.lostandfound.app.dto.request.CommentRequest;
import com.lostandfound.app.dto.response.CommentResponse;
import com.lostandfound.app.security.CustomUserDetails;

import java.util.List;

public interface CommentService {

    CommentResponse createComment(CommentRequest.CreateComment request, CustomUserDetails currentUser);

    List<CommentResponse> getCommentsByPost(Long postId);

    CommentResponse updateComment(Long commentId, CommentRequest.UpdateComment request, CustomUserDetails currentUser);

    void deleteComment(Long commentId, CustomUserDetails currentUser);
}
