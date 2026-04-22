package com.lostandfound.app.controller;

import com.lostandfound.app.annotation.ApiId;
import com.lostandfound.app.annotation.CheckSecurity;
import com.lostandfound.app.annotation.CurrentUser;
import com.lostandfound.app.dto.request.CommentRequest;
import com.lostandfound.app.dto.response.BaseResponse;
import com.lostandfound.app.dto.response.CommentResponse;
import com.lostandfound.app.security.CustomUserDetails;
import com.lostandfound.app.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
@Tag(name = "4. Comment Management", description = "Endpoints for creating, updating, and managing comments on posts")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @ApiId("CMT-001")
    @Operation(summary = "Create Comment", description = "Allows an authenticated user to post a comment on a specific post.")
    public ResponseEntity<BaseResponse<CommentResponse>> create(
            @Valid @RequestBody CommentRequest.CreateComment request,
            @Parameter(hidden = true) @CurrentUser CustomUserDetails currentUser) {

        log.info("REST request to create comment by user ID: {} for post ID: {}", currentUser.getId(), request.postId());
        CommentResponse response = commentService.createComment(request, currentUser);
        return BaseResponse.success("Comment created successfully", response);
    }

    @GetMapping("/post/{postId}")
    @CheckSecurity.Public.canRead
    @ApiId("CMT-002")
    @Operation(summary = "Get Comments by Post", description = "Fetches all active comments for a given post ID.")
    public ResponseEntity<BaseResponse<List<CommentResponse>>> getByPost(
            @PathVariable Long postId) {

        log.info("REST request to get comments for post ID: {}", postId);
        List<CommentResponse> response = commentService.getCommentsByPost(postId);
        return BaseResponse.success("Comments fetched successfully", response);
    }

    @PutMapping("/{id}")
    @CheckSecurity.Comments.canManage
    @ApiId("CMT-003")
    @Operation(summary = "Update Comment", description = "Updates the content of an existing comment. Only the author can update.")
    public ResponseEntity<BaseResponse<CommentResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest.UpdateComment request,
            @Parameter(hidden = true) @CurrentUser CustomUserDetails currentUser) {

        log.info("REST request to update comment ID: {} by user ID: {}", id, currentUser.getId());
        CommentResponse response = commentService.updateComment(id, request, currentUser);
        return BaseResponse.success("Comment updated successfully", response);
    }

    @DeleteMapping("/{id}")
    @CheckSecurity.Comments.canManage
    @ApiId("CMT-004")
    @Operation(summary = "Delete Comment", description = "Soft deletes a comment. Only the author or an admin can delete.")
    public ResponseEntity<BaseResponse<Void>> delete(
            @PathVariable Long id,
            @Parameter(hidden = true) @CurrentUser CustomUserDetails currentUser) {

        log.info("REST request to delete comment ID: {} by user ID: {}", id, currentUser.getId());
        commentService.deleteComment(id, currentUser);
        return BaseResponse.success("Comment has been deleted successfully");
    }

    @GetMapping("/{id}")
    @ApiId("CMT-005")
    @Operation(summary = "Get Comment by ID", description = "Fetches a single comment by its ID.")
    public ResponseEntity<BaseResponse<CommentResponse>> getCommentById(@PathVariable Long id) {

        log.info("REST request to fetch comment ID: {}", id);
        CommentResponse response = commentService.getCommentById(id);
        return BaseResponse.success("Comment fetched successfully", response);
    }
}