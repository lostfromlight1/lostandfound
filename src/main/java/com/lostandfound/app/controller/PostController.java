package com.lostandfound.app.controller;

import com.lostandfound.app.annotation.ApiId;
import com.lostandfound.app.annotation.CheckSecurity;
import com.lostandfound.app.dto.request.PostRequest;
import com.lostandfound.app.dto.response.BaseResponse;
import com.lostandfound.app.dto.response.PageResponse;
import com.lostandfound.app.dto.response.PostResponse;
import com.lostandfound.app.model.MyanmarCity;
import com.lostandfound.app.model.PostType;
import com.lostandfound.app.security.CustomUserDetails;
import com.lostandfound.app.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Tag(name = "3. Post Management", description = "Endpoints for creating, filtering, and managing lost and found posts")
public class PostController {

    private final PostService postService;

    @PostMapping("/create")
    @CheckSecurity.Authenticated.isRequired
    @ApiId("PST-001")
    public ResponseEntity<BaseResponse<PostResponse.PostDto>> createPost(
            @Valid @RequestBody PostRequest.CreatePostRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("REST request to create post by user ID: {}", userDetails.getId());
        PostResponse.PostDto response = postService.createPost(request, userDetails);
        return BaseResponse.created("Post created successfully", response);
    }

    @PutMapping("/{id}")
    @CheckSecurity.Posts.canManage
    @ApiId("PST-002")
    @Operation(summary = "Update Post", description = "Updates an existing post. Only the post owner can perform this action.")
    public ResponseEntity<BaseResponse<PostResponse.PostDto>> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest.UpdatePostRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) { // Fixed: Use @AuthenticationPrincipal

        log.info("REST request to update post ID: {} by user ID: {}", id, userDetails.getId());
        PostResponse.PostDto response = postService.updatePost(id, request, userDetails);
        return BaseResponse.success("Post updated successfully", response);
    }

    @GetMapping
    @CheckSecurity.Public.canRead
    @ApiId("PST-003")
    @Operation(summary = "Get All Posts", description = "Fetches a paginated list of posts with optional filtering by type, category, and location.")
    public ResponseEntity<BaseResponse<PageResponse<PostResponse.PostDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) PostType type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) MyanmarCity location) {

        log.info("REST request to fetch posts list. Page: {}, Size: {}", page, size);
        PageResponse<PostResponse.PostDto> response = postService.getAll(page, size, type, categoryId, location);
        return BaseResponse.success("Posts fetched successfully", response);
    }

    @DeleteMapping("/{id}")
    @CheckSecurity.Posts.canManage
    @ApiId("PST-004")
    @Operation(summary = "Delete Post", description = "Soft deletes a post. Only the post owner can perform this action.")
    public ResponseEntity<BaseResponse<Void>> deletePost(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) { // Fixed: Use @AuthenticationPrincipal

        log.info("REST request to delete post ID: {} by user ID: {}", id, userDetails.getId());
        postService.deletePost(id, userDetails);
        return BaseResponse.success("Post deleted successfully");
    }
}