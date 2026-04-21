package com.lostandfound.app.controller;

import com.lostandfound.app.annotation.CurrentUser;
import com.lostandfound.app.dto.response.BaseResponse;
import com.lostandfound.app.security.CustomUserDetails;
import com.lostandfound.app.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/posts")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;



    @PostMapping("/{postId}/like")
    public ResponseEntity<BaseResponse<Void>> toggleLike(
            @PathVariable Long  postId,
            @CurrentUser CustomUserDetails userDetails
            ){

        postLikeService.toggleLike(postId,userDetails);
        return BaseResponse.success("Toggle Like Successful");

    }
}
