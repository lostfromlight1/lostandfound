package com.lostandfound.app.serviceimpl;

import com.lostandfound.app.dto.request.PostRequest;
import com.lostandfound.app.dto.response.PageResponse;
import com.lostandfound.app.dto.response.PostResponse;
import com.lostandfound.app.model.*;
import com.lostandfound.app.repository.CategoryRepository;
import com.lostandfound.app.repository.PostRepository;
import com.lostandfound.app.repository.UserRepository;
import com.lostandfound.app.security.CustomUserDetails;
import com.lostandfound.app.service.PostService;
import com.lostandfound.app.util.PostSpecification;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;

    @Override
    public PostResponse.PostDto createPost(PostRequest.CreatePostRequest request, CustomUserDetails userDetailsService) {

        User user = userRepository.findById(userDetailsService.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new RuntimeException("category Not Found"));
        Post post = new Post();
        post.setTitle(request.title());
        post.setDescription(request.description());
        post.setPostType(request.type());
        post.setLocation(MyanmarCity.valueOf(request.location().toUpperCase()));
        post.setLostFoundDate(request.lostFoundDate());
        post.setContactInfo(request.contactInfo());
        post.setReward(request.reward());
        post.setStatus(PostStatus.OPEN);


        post.setUser(user);
        post.setCategory(category);

        postRepository.save(post);

        return mapToDto(post);

    }

    @Transactional
    @Override
    public PostResponse.PostDto updatePost(Long id, PostRequest.UpdatePostRequest request, CustomUserDetails userDetails) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));


        if (!post.getUser().getId().equals(userDetails.getId())) {
            throw new RuntimeException("You are not allowed to update this post");
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));


        post.setTitle(request.title());
        post.setDescription(request.description());
        post.setPostType(request.type());
        post.setStatus(request.status());
        post.setLocation(MyanmarCity.valueOf(request.location().toUpperCase()));;
        post.setContactInfo(request.contactInfo());
        post.setLostFoundDate(request.lostFoundDate());
        post.setCategory(category);

        postRepository.save(post);

        return mapToDto(post);

    }
    @Transactional(readOnly = true)
    @Override
    public PageResponse<PostResponse.PostDto> getAll(
            int page,
            int size,
            PostType type,
            Long categoryId,
            MyanmarCity location
    ) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Specification<Post> spec = Specification
                .where(PostSpecification.hasType(type))
                .and(PostSpecification.hasCategory(categoryId))
                .and(PostSpecification.hasLocation(location));

        Page<Post> postPage = postRepository.findAll(spec, pageable);

        // ✅ FIX HERE
        List<PostResponse.PostDto> content = postPage.getContent()
                .stream()
                .map(this::mapToDto) // map Post → PostDto
                .toList();

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
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // ✅ ownership check
        if (!post.getUser().getId().equals(userDetails.getId())) {
            throw new RuntimeException("You are not allowed to delete this post");
        }

        // ✅ soft delete
        post.softDelete();

        postRepository.save(post);
    }


    // 🔄 mapper
    private PostResponse.PostDto mapToDto(Post post) {
        return new PostResponse.PostDto(
                post.getId(),
                post.getTitle(),
                post.getDescription(),
                post.getPostType(),
                post.getStatus(),
                post.getLocation().name(),
                post.getLostFoundDate(),
                post.getContactInfo(),
                post.getReward(),

                new PostResponse.UserSummary(
                        post.getUser().getId(),
                        post.getUser().getDisplayName()
                ),

                new PostResponse.CategoryDto(
                        post.getCategory().getId(),
                        post.getCategory().getName()
                )
        );
    }
}
