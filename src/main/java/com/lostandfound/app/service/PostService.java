package com.lostandfound.app.service;


import com.lostandfound.app.dto.request.PostRequest;
import com.lostandfound.app.dto.response.PageResponse;
import com.lostandfound.app.dto.response.PostResponse;
import com.lostandfound.app.model.MyanmarCity;
import com.lostandfound.app.model.PostType;
import com.lostandfound.app.security.CustomUserDetails;

import java.time.LocalDate;

public interface PostService {

PostResponse.PostDto createPost(PostRequest.CreatePostRequest request, CustomUserDetails userDetailsService);

PostResponse.PostDto updatePost(Long id, PostRequest.UpdatePostRequest request,CustomUserDetails userDetails );

    public PageResponse<PostResponse.PostDto> getAll(
            int page,
            int size,
            PostType type,
            Long categoryId,
            MyanmarCity city,
            String locationDetails,
            LocalDate startDate,
            LocalDate endDate,
            CustomUserDetails userDetails


    );

    PageResponse<PostResponse.PostDto> getUserPosts(Long userId, int page, int size);
    public void deletePost(Long id,CustomUserDetails userDetails);

}
