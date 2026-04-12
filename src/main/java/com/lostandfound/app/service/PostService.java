package com.lostandfound.app.service;


import com.lostandfound.app.dto.request.PostRequest;
import com.lostandfound.app.dto.response.PageResponse;
import com.lostandfound.app.dto.response.PostResponse;
import com.lostandfound.app.model.MyanmarCity;
import com.lostandfound.app.model.PostType;
import com.lostandfound.app.security.CustomUserDetails;

public interface PostService {

PostResponse.PostDto createPost(PostRequest.CreatePostRequest request, CustomUserDetails userDetailsService);

PostResponse.PostDto updatePost(Long id, PostRequest.UpdatePostRequest request,CustomUserDetails userDetails );

    public PageResponse<PostResponse.PostDto> getAll(
            int page,
            int size,
            PostType type,
            Long categoryId,
            MyanmarCity location
    );

    public void deletePost(Long id,CustomUserDetails userDetails);

}
