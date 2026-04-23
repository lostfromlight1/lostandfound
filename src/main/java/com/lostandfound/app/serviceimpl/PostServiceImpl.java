package com.lostandfound.app.serviceimpl;

import com.lostandfound.app.dto.request.PostRequest;
import com.lostandfound.app.dto.response.PageResponse;
import com.lostandfound.app.dto.response.PostResponse;
import com.lostandfound.app.exception.AppException;
import com.lostandfound.app.exception.ErrorCode;
import com.lostandfound.app.model.*;
import com.lostandfound.app.repository.*;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostBookmarkRepository postBookmarkRepository; // <--- INJECTED

    @Override
    @Transactional
    public PostResponse.PostDto createPost(PostRequest.CreatePostRequest request,
                                           CustomUserDetails userDetailsService) {
        Long userId = userDetailsService.getId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Category not found"));

        try {
            Post post = buildNewPost(request, user, category);
            Post savedPost = postRepository.save(post);
            return mapToDto(savedPost, userId);
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Invalid location or post type provided");
        } catch (Exception e) {
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

        if (post.getImages() == null) {
            post.setImages(new ArrayList<>());
        } else {
            post.getImages().clear();
        }

        if (request.images() != null && !request.images().isEmpty()) {
            List<PostImage> newImages = request.images().stream().map(imgReq -> {
                PostImage img = new PostImage();
                img.setImageUrl(imgReq.url());
                img.setPublicId(imgReq.publicId());
                img.setSortOrder(imgReq.sortOrder() != null ? imgReq.sortOrder() : 0);
                img.setPost(post);
                return img;
            }).toList();
            post.getImages().addAll(newImages);
        }

        Post updatedPost = postRepository.save(post);
        return mapToDto(updatedPost, userId);
    }

    @Transactional(readOnly = true)
    @Override
    public PostResponse.PostDto getPostById(Long id, CustomUserDetails userDetails) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Post not found"));

        Long currentUserId = (userDetails != null) ? userDetails.getId() : null;
        return mapToDto(post, currentUserId);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<PostResponse.PostDto> getAll(
            int page, int size, String sortBy, PostType type, Long categoryId,
            MyanmarCity city, String locationDetails, LocalDate startDate, LocalDate endDate,
            CustomUserDetails userDetails
    ) {
        Sort sort;
        if ("TOP".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "likeCount").and(Sort.by(Sort.Direction.DESC, "createdAt"));
        } else {
            sort = Sort.by(Sort.Direction.DESC, "createdAt");
        }

        Pageable pageable = PageRequest.of(page, size, sort);

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

    // -------------------------------------------------------------------------
    // NEW BOOKMARK METHODS
    // -------------------------------------------------------------------------

    @Transactional
    @Override
    public void toggleBookmark(Long postId, CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Post not found"));

        Optional<PostBookmark> existingBookmark = postBookmarkRepository.findByUserIdAndPostId(userId, postId);

        if (existingBookmark.isPresent()) {
            postBookmarkRepository.delete(existingBookmark.get());
        } else {
            User user = userRepository.getReferenceById(userId);
            PostBookmark newBookmark = PostBookmark.builder()
                    .user(user)
                    .post(post)
                    .build();
            postBookmarkRepository.save(newBookmark);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<PostResponse.PostDto> getBookmarkedPosts(CustomUserDetails userDetails, int page, int size) {
        Long userId = userDetails.getId();
        Pageable pageable = PageRequest.of(page, size);

        Page<Post> postPage = postBookmarkRepository.findBookmarkedPostsByUserId(userId, pageable);

        List<PostResponse.PostDto> content = postPage.getContent()
                .stream()
                .map(post -> mapToDto(post, userId))
                .toList();

        return new PageResponse<>(content, postPage.getNumber(), postPage.getSize(), postPage.getTotalElements(), postPage.getTotalPages());
    }

    // -------------------------------------------------------------------------

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

        post.setImages(new ArrayList<>());

        if (request.images() != null && !request.images().isEmpty()) {
            List<PostImage> newImages = request.images().stream().map(imgReq -> {
                PostImage img = new PostImage();
                img.setImageUrl(imgReq.url());
                img.setPublicId(imgReq.publicId());
                img.setSortOrder(imgReq.sortOrder() != null ? imgReq.sortOrder() : 0);
                img.setPost(post);
                return img;
            }).toList();
            post.getImages().addAll(newImages);
        }

        return post;
    }

    private PostResponse.PostDto mapToDto(Post post, Long currentUserId) {
        long likes = post.getLikeCount() != null ? post.getLikeCount() : 0L;
        long comments = post.getCommentCount() != null ? post.getCommentCount() : 0L;

        boolean liked = (currentUserId != null) && postLikeRepository.existsByUserIdAndPostId(currentUserId, post.getId());

        // <--- NEW BOOKMARK FLAG
        boolean bookmarked = (currentUserId != null) && postBookmarkRepository.existsByUserIdAndPostId(currentUserId, post.getId());

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
                likes,
                liked,
                comments,
                bookmarked // <--- ADDED
        );
    }

    private String getTraceId() {
        return Objects.toString(MDC.get("traceId"), "SYSTEM");
    }
}