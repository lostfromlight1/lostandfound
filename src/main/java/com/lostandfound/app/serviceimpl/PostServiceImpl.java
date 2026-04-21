package com.lostandfound.app.serviceimpl;

import com.lostandfound.app.dto.request.PostRequest;
import com.lostandfound.app.dto.response.PageResponse;
import com.lostandfound.app.dto.response.PostResponse;
import com.lostandfound.app.exception.AppException;
import com.lostandfound.app.exception.ErrorCode;
import com.lostandfound.app.model.*;
import com.lostandfound.app.repository.CategoryRepository;
import com.lostandfound.app.repository.PostRepository;
import com.lostandfound.app.repository.UserRepository;
import com.lostandfound.app.security.CustomUserDetails;
import com.lostandfound.app.service.ImageService;
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
    private final ImageService imageService;

    @Override
    @Transactional
    public PostResponse.PostDto createPost(PostRequest.CreatePostRequest request, CustomUserDetails userDetailsService) {
        log.info("[{}] Attempting to create post for user ID: {}", getTraceId(), userDetailsService.getId());

        User user = userRepository.findById(userDetailsService.getId())
                .orElseThrow(() -> {
                    log.error("[{}] User not found during post creation: {}", getTraceId(), userDetailsService.getId());
                    return new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User not found");
                });

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> {
                    log.error("[{}] Category not found: {}", getTraceId(), request.categoryId());
                    return new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Category not found");
                });

        try {
            Post post = new Post();
            post.setTitle(request.title());
            post.setDescription(request.description());
            post.setPostType(request.type());
            post.setCity(request.city());
            post.setLocationDetails(request.locationDetails());
            post.setLostFoundDate(request.lostFoundDate());
            post.setContactInfo(request.contactInfo());
            post.setReward(request.reward());
            post.setStatus(PostStatus.OPEN);
            post.setUser(user);
            post.setCategory(category);

            if (request.images() != null && !request.images().isEmpty()) {
                request.images().forEach(imgReq -> {
                    PostImage postImage = PostImage.builder()
                            .imageUrl(imgReq.url())
                            .publicId(imgReq.publicId())
                            .sortOrder(imgReq.sortOrder() != null ? imgReq.sortOrder() : 0)
                            .build();
                    post.addImage(postImage);
                });
            }

            Post savedPost = postRepository.save(post);
            log.info("[{}] Successfully created post ID: {}", getTraceId(), savedPost.getId());
            return mapToDto(savedPost);

        } catch (IllegalArgumentException e) {
            log.error("[{}] Invalid enum mapping (likely city/location): {}", getTraceId(), e.getMessage());
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Invalid location or post type provided");
        } catch (Exception e) {
            log.error("[{}] Unexpected error creating post: {}", getTraceId(), e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_ERROR, "An error occurred while creating the post");
        }
    }

    @Transactional
    @Override
    public PostResponse.PostDto updatePost(Long id, PostRequest.UpdatePostRequest request, CustomUserDetails userDetails) {
        log.info("[{}] Attempting to update post ID: {} by user ID: {}", getTraceId(), id, userDetails.getId());

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Post not found"));

        if (!post.getUser().getId().equals(userDetails.getId())) {
            log.warn("[{}] User ID: {} attempted to modify post ID: {} without permission", getTraceId(), userDetails.getId(), id);
            throw new AppException(ErrorCode.POST_MODIFICATION_DENIED, "You are not allowed to update this post");
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Category not found"));

        try {
            post.setTitle(request.title());
            post.setDescription(request.description());
            post.setPostType(request.type());
            post.setStatus(request.status());
            post.setCity(request.city());
            post.setLocationDetails(request.locationDetails());
            post.setContactInfo(request.contactInfo());
            post.setLostFoundDate(request.lostFoundDate());
            post.setCategory(category);

            if (request.images() != null) {
                List<String> incomingPublicIds = request.images().stream()
                        .map(PostRequest.ImageRequest::publicId)
                        .toList();

                List<PostImage> imagesToDelete = post.getImages().stream()
                        .filter(existingImage -> !incomingPublicIds.contains(existingImage.getPublicId()))
                        .toList();

                imagesToDelete.forEach(img -> {
                    log.info("[{}] Deleting removed image from Cloudinary: {}", getTraceId(), img.getPublicId());
                    imageService.deleteImage(img.getPublicId());
                });

                post.getImages().clear();
                request.images().forEach(imgReq -> {
                    PostImage postImage = PostImage.builder()
                            .imageUrl(imgReq.url())
                            .publicId(imgReq.publicId())
                            .sortOrder(imgReq.sortOrder() != null ? imgReq.sortOrder() : 0)
                            .build();
                    post.addImage(postImage);
                });
            }

            Post updatedPost = postRepository.save(post);
            log.info("[{}] Successfully updated post ID: {}", getTraceId(), updatedPost.getId());
            return mapToDto(updatedPost);

        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Invalid location or status provided");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<PostResponse.PostDto> getAll(
            int page,
            int size,
            PostType type,
            Long categoryId,
            MyanmarCity city,
            String locationDetails
    ) {
        log.info("[{}] Fetching posts. Page: {}, Size: {}", getTraceId(), page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Specification<Post> spec = Specification.allOf(
                PostSpecification.hasType(type),
                PostSpecification.hasCategory(categoryId),
                PostSpecification.hasCity(city),
                PostSpecification.hasLocationDetails(locationDetails)
        );

        Page<Post> postPage = postRepository.findAll(spec, pageable);

        List<PostResponse.PostDto> content = postPage.getContent()
                .stream()
                .map(this::mapToDto)
                .toList();

        return new PageResponse<>(
                content,
                postPage.getNumber(),
                postPage.getSize(),
                postPage.getTotalElements(),
                postPage.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<PostResponse.PostDto> getUserPosts(Long userId, int page, int size) {
        log.info("[{}] Fetching posts for user ID: {}. Page: {}, Size: {}", getTraceId(), userId, page, size);


        boolean userExists = userRepository.existsById(userId);
        if (!userExists) {
            log.error("[{}] User not found with ID: {}", getTraceId(), userId);
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User not found with id: " + userId);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Post> postPage = postRepository.findPostsByUserId(userId, pageable);

        List<PostResponse.PostDto> content = postPage.getContent()
                .stream()
                .map(this::mapToDto)
                .toList();

        log.info("[{}] Successfully fetched {} posts for user ID: {}", getTraceId(), content.size(), userId);

        return new PageResponse<>(
                content,
                postPage.getNumber(),
                postPage.getSize(),
                postPage.getTotalElements(),
                postPage.getTotalPages()
        );
    }

    @Transactional
    @Override
    public void deletePost(Long id, CustomUserDetails userDetails) {
        log.info("[{}] Attempting to delete post ID: {} by user ID: {}", getTraceId(), id, userDetails.getId());

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Post not found"));

        if (!post.getUser().getId().equals(userDetails.getId())) {
            log.warn("[{}] User ID: {} attempted to delete post ID: {} without permission", getTraceId(), userDetails.getId(), id);
            throw new AppException(ErrorCode.POST_MODIFICATION_DENIED, "You are not allowed to delete this post");
        }

        try {
            post.getImages().forEach(img -> {
                log.info("[{}] Deleting image for deleted post from Cloudinary: {}", getTraceId(), img.getPublicId());
                imageService.deleteImage(img.getPublicId());
            });

            post.softDelete();
            postRepository.save(post);
            log.info("[{}] Successfully deleted post ID: {}", getTraceId(), id);

        } catch (Exception e) {
            log.error("[{}] Error deleting post ID: {}. Details: {}", getTraceId(), id, e.getMessage());
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to delete post");
        }
    }

    private PostResponse.PostDto mapToDto(Post post) {
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
                post.getLostFoundDate(),
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
                imageDtos
        );
    }

    private String getTraceId() {
        return Objects.toString(MDC.get("traceId"), "SYSTEM");
    }
}