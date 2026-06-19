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

    PageResponse<PostResponse.PostDto> getAll(
            int page,
            int size,
            String sortBy,
            PostType type,
            Long categoryId,
            MyanmarCity city,
            String locationDetails,
            LocalDate startDate,
            LocalDate endDate,
            CustomUserDetails userDetails
    );

    PageResponse<PostResponse.PostDto> getUserPosts(Long userId, int page, int size);

    void deletePost(Long id,CustomUserDetails userDetails);

    PostResponse.PostDto getPostById(Long id, CustomUserDetails userDetails);

    void toggleBookmark(Long postId, CustomUserDetails userDetails);

    PageResponse<PostResponse.PostDto> getBookmarkedPosts(CustomUserDetails userDetails, int page, int size);
}