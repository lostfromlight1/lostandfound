package com.lostandfound.app.service;

import com.lostandfound.app.security.CustomUserDetails;

public interface PostLikeService {

    public void toggleLike(Long postId, CustomUserDetails customUserDetails);
}
