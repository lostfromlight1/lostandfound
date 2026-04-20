package com.lostandfound.app.controller;

import com.lostandfound.app.annotation.ApiId;
import com.lostandfound.app.annotation.CurrentUser;
import com.lostandfound.app.dto.request.ReplyRequest;
import com.lostandfound.app.dto.response.BaseResponse;
import com.lostandfound.app.dto.response.ReplyResponse;
import com.lostandfound.app.security.CustomUserDetails;
import com.lostandfound.app.service.ReplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/replies")
@RequiredArgsConstructor
@Tag(name = "5. Reply Management", description = "Endpoints for creating, updating, and managing replies on comments")
public class ReplyController {

    private final ReplyService replyService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ApiId("REP-001")
    @Operation(summary = "Create Reply", description = "Allows an authenticated user to post a reply to a comment or another reply.")
    public ResponseEntity<BaseResponse<ReplyResponse>> create(
            @Valid @RequestBody ReplyRequest.CreateReply request,
            @Parameter(hidden = true) @CurrentUser CustomUserDetails currentUser) {

        log.info("REST request to create reply by user ID: {} for comment ID: {}", currentUser.getId(), request.commentId());
        ReplyResponse response = replyService.createReply(request, currentUser);
        return BaseResponse.success("Reply created successfully", response);
    }

    @GetMapping("/comment/{commentId}")
    @ApiId("REP-002")
    @Operation(summary = "Get Replies by Comment", description = "Fetches all active replies for a given comment ID (includes nested replies).")
    public ResponseEntity<BaseResponse<List<ReplyResponse>>> getByComment(
            @PathVariable Long commentId) {

        log.info("REST request to get replies for comment ID: {}", commentId);
        List<ReplyResponse> response = replyService.getRepliesByComment(commentId);
        return BaseResponse.success("Replies fetched successfully", response);
    }

    @GetMapping("/comment/{commentId}/root")
    @ApiId("REP-003")
    @Operation(summary = "Get Root Replies", description = "Fetches only root level replies (direct replies to comment, not nested).")
    public ResponseEntity<BaseResponse<List<ReplyResponse>>> getRootByComment(
            @PathVariable Long commentId) {

        log.info("REST request to get root replies for comment ID: {}", commentId);
        List<ReplyResponse> response = replyService.getRootRepliesByComment(commentId);
        return BaseResponse.success("Root replies fetched successfully", response);
    }

    @GetMapping("/{replyId}/nested")
    @ApiId("REP-004")
    @Operation(summary = "Get Nested Replies", description = "Fetches all replies that are nested under a specific reply.")
    public ResponseEntity<BaseResponse<List<ReplyResponse>>> getNested(
            @PathVariable Long replyId) {

        log.info("REST request to get nested replies for reply ID: {}", replyId);
        List<ReplyResponse> response = replyService.getNestedReplies(replyId);
        return BaseResponse.success("Nested replies fetched successfully", response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @ApiId("REP-005")
    @Operation(summary = "Update Reply", description = "Updates the content of an existing reply. Only the author can update.")
    public ResponseEntity<BaseResponse<ReplyResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ReplyRequest.UpdateReply request,
            @Parameter(hidden = true) @CurrentUser CustomUserDetails currentUser) {

        log.info("REST request to update reply ID: {} by user ID: {}", id, currentUser.getId());
        ReplyResponse response = replyService.updateReply(id, request, currentUser);
        return BaseResponse.success("Reply updated successfully", response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @ApiId("REP-006")
    @Operation(summary = "Delete Reply", description = "Soft deletes a reply. Only the author or an admin can delete.")
    public ResponseEntity<BaseResponse<Void>> delete(
            @PathVariable Long id,
            @Parameter(hidden = true) @CurrentUser CustomUserDetails currentUser) {

        log.info("REST request to delete reply ID: {} by user ID: {}", id, currentUser.getId());
        replyService.deleteReply(id, currentUser);
        return BaseResponse.success("Reply has been deleted successfully");
    }
}
