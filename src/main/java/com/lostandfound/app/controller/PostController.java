package com.lostandfound.app.controller;


import com.lostandfound.app.annotation.CurrentUser;
import com.lostandfound.app.dto.request.PostRequest;
import com.lostandfound.app.dto.response.BaseResponse;
import com.lostandfound.app.dto.response.PageResponse;
import com.lostandfound.app.dto.response.PostResponse;
import com.lostandfound.app.model.MyanmarCity;
import com.lostandfound.app.model.PostType;
import com.lostandfound.app.security.CustomUserDetails;
import com.lostandfound.app.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/post")
@Slf4j
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping("/create")
    public ResponseEntity<BaseResponse<PostResponse.PostDto>> createPost(
            @Valid @RequestBody PostRequest.CreatePostRequest request,
            @CurrentUser CustomUserDetails userDetails
    ) {
        PostResponse.PostDto response = postService.createPost(request, userDetails);

        return BaseResponse.created("create post successful", response);
    }



    @PutMapping("/update/{id}")
    public ResponseEntity<BaseResponse<PostResponse.PostDto>> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest.UpdatePostRequest request,
            @CurrentUser CustomUserDetails userDetails
    ) {
        PostResponse.PostDto response = postService.updatePost(id,request, userDetails);

        return BaseResponse.created("update post successful", response);
    }

  @GetMapping
public ResponseEntity<BaseResponse<PageResponse<PostResponse.PostDto>>> getAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "5") int size,
        @RequestParam(required = false) PostType type,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) MyanmarCity location
) {
          PageResponse<PostResponse.PostDto> response=postService.getAll(page, size, type, categoryId, location);

          return BaseResponse.success("Fetch Post Successful",response);

}

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deletePost(
            @PathVariable Long id,
            @CurrentUser CustomUserDetails user
    ) {
        postService.deletePost(id, user);
        return BaseResponse.success("Post deleted successfully");
    }



}
