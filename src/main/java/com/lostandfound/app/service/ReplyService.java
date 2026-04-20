package com.lostandfound.app.service;

import com.lostandfound.app.dto.request.ReplyRequest;
import com.lostandfound.app.dto.response.ReplyResponse;
import com.lostandfound.app.security.CustomUserDetails;

import java.util.List;

public interface ReplyService {

    ReplyResponse createReply(ReplyRequest.CreateReply request, CustomUserDetails currentUser);

    /**
     * Get all replies for a specific comment (with nested replies)
     */
    List<ReplyResponse> getRepliesByComment(Long commentId);

    /**
     * Get root level replies only (replies directly to comment, not nested)
     */
    List<ReplyResponse> getRootRepliesByComment(Long commentId);

    /**
     * Get nested replies for a specific reply
     */
    List<ReplyResponse> getNestedReplies(Long replyId);

    /**
     * Update an existing reply
     */
    ReplyResponse updateReply(Long replyId, ReplyRequest.UpdateReply request, CustomUserDetails currentUser);

    /**
     * Delete a reply (soft delete)
     */
    void deleteReply(Long replyId, CustomUserDetails currentUser);
}
