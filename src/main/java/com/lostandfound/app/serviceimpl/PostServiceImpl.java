package com.lostandfound.app.serviceimpl;

import com.lostandfound.app.dto.request.PostRequest;
import com.lostandfound.app.dto.response.PageResponse;
import com.lostandfound.app.dto.response.PostResponse;
import com.lostandfound.app.exception.AppException;
import com.lostandfound.app.exception.ErrorCode;
import com.lostandfound.app.model.*;
import com.lostandfound.app.repository.CategoryRepository;
import com.lostandfound.app.repository.PostLikeRepository;
import com.lostandfound.app.repository.PostRepository;
import com.lostandfound.app.repository.UserRepository;
import com.lostandfound.app.security.CustomUserDetails;
import com.lostandfound.app.service.PostService;
import com.lostandfound.app.util.PostSpecification;
import org.slf4j.MDC;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Comparator;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    @Override
    @Transactional
    public PostResponse.PostDto createPost(PostRequest.CreatePostRequest request,
                                           CustomUserDetails userDetailsService) {

        Long userId = userDetailsService.getId();
        log.info("[{}] Attempting to create post for user ID: {}", getTraceId(), userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Category not found"));

        try {
            Post post = buildNewPost(request, user, category);
            Post savedPost = postRepository.save(post);
            log.info("[{}] Successfully created post ID: {}", getTraceId(), savedPost.getId());
            return mapToDto(savedPost, userId);

        } catch (IllegalArgumentException e) {
            log.error("[{}] Invalid enum mapping: {}", getTraceId(), e.getMessage());
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Invalid location or post type provided");
        } catch (Exception e) {
            log.error("[{}] Unexpected error: {}", getTraceId(), e.getMessage());
            throw new AppException(ErrorCode.INTERNAL_ERROR, "An error occurred while creating the post");
        }
    }

    @Transactional
    @Override
    public PostResponse.PostDto updatePost(Long id, PostRequest.UpdatePostRequest request,
                                           CustomUserDetails userDetails) {

        Long userId = userDetails.getId();
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Post not found"));

        if (!post.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.POST_MODIFICATION_DENIED, "You are not allowed to update this post");
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Category not found"));

        post.setTitle(request.title());
        post.setDescription(request.description());
        post.setPostType(request.type());
        post.setStatus(request.status());
        post.setCity(request.city());
        post.setLocationDetails(request.locationDetails());
        post.setLatitude(request.latitude());
        post.setLongitude(request.longitude());
        post.setContactInfo(request.contactInfo());
        post.setLostFoundDate(request.lostFoundDate());
        post.setCategory(category);

        // Image update logic remains here...

        Post updatedPost = postRepository.save(post);
        return mapToDto(updatedPost, userId);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<PostResponse.PostDto> getAll(
            int page, int size, PostType type, Long categoryId,
            MyanmarCity city, String locationDetails, LocalDate startDate, LocalDate endDate,
            CustomUserDetails userDetails
    ) {
        log.info("[{}] Fetching posts. Page: {}, Size: {}", getTraceId(), page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Specification<Post> spec = Specification.allOf(
                PostSpecification.hasType(type),
                PostSpecification.hasCategory(categoryId),
                PostSpecification.hasCity(city),
                PostSpecification.hasLocationDetails(locationDetails),
                PostSpecification.hasLostFoundBetween(startDate, endDate)
        );

        Page<Post> postPage = postRepository.findAll(spec, pageable);

        Long currentUserId = (userDetails != null) ? userDetails.getId() : null;

        List<PostResponse.PostDto> content = postPage.getContent()
                .stream()
                .map(post -> mapToDto(post, currentUserId))
                .toList();

        return new PageResponse<>(content, postPage.getNumber(), postPage.getSize(), postPage.getTotalElements(), postPage.getTotalPages());
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<PostResponse.PostDto> getUserPosts(Long userId, int page, int size) {
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User not found");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> postPage = postRepository.findPostsByUserId(userId, pageable);

        return new PageResponse<>(
                postPage.getContent().stream().map(post -> mapToDto(post, userId)).toList(),
                postPage.getNumber(), postPage.getSize(), postPage.getTotalElements(), postPage.getTotalPages()
        );
    }

    @Transactional
    @Override
    public void deletePost(Long id, CustomUserDetails userDetails) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Post not found"));

        if (!post.getUser().getId().equals(userDetails.getId())) {
            throw new AppException(ErrorCode.POST_MODIFICATION_DENIED, "Permission denied");
        }

        post.softDelete();
        postRepository.save(post);
    }

    private Post buildNewPost(PostRequest.CreatePostRequest request, User user, Category category) {
        Post post = new Post();
        post.setTitle(request.title());
        post.setDescription(request.description());
        post.setPostType(request.type());
        post.setCity(request.city());
        post.setLocationDetails(request.locationDetails());
        post.setLatitude(request.latitude());
        post.setLongitude(request.longitude());
        post.setLostFoundDate(request.lostFoundDate());
        post.setContactInfo(request.contactInfo());
        post.setReward(request.reward());
        post.setStatus(PostStatus.OPEN);
        post.setUser(user);
        post.setCategory(category);
        return post;
    }

    private PostResponse.PostDto mapToDto(Post post, Long currentUserId) {
        long likeCount = postLikeRepository.countByPostId(post.getId());
        boolean liked = (currentUserId != null) && postLikeRepository.existsByUserIdAndPostId(currentUserId, post.getId());

        List<PostResponse.ImageDto> imageDtos = post.getImages().stream()
                .sorted(Comparator.comparingInt(PostImage::getSortOrder))
                .map(img -> new PostResponse.ImageDto(img.getId(), img.getImageUrl(), img.getSortOrder()))
                .toList();

        return new PostResponse.PostDto(
                post.getId(),
                post.getTitle(),
                post.getDescription(),
                post.getPostType(),
                post.getStatus(),
                post.getCity(),
                post.getLocationDetails(),
                post.getLatitude(),
                post.getLongitude(),
                post.getLostFoundDate(),
                post.getCreatedAt(),
                post.getContactInfo(),
                post.getReward(),
                new PostResponse.UserSummary(
                        post.getUser().getId(),
                        post.getUser().getDisplayName(),
                        post.getUser().getAvatarUrl(),
                        post.getUser().getEmail()
                ),
                new PostResponse.CategoryDto(
                        post.getCategory().getId(),
                        post.getCategory().getName()
                ),
                imageDtos,
                likeCount,
                liked
        );
    }

    private String getTraceId() {
        return Objects.toString(MDC.get("traceId"), "SYSTEM");
    }
}