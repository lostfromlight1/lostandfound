package com.lostandfound.app.repository;

import com.lostandfound.app.model.Post;
import com.lostandfound.app.model.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post,Long> , JpaSpecificationExecutor<Post> {

    @Query("""
        SELECT p FROM Post p
        WHERE p.user.id = :userId AND p.active = true
        ORDER BY p.createdAt DESC
    """)
    Page<Post> findPostsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("""
        SELECT COUNT(p) FROM Post p
        WHERE p.user.id = :userId AND p.active = true
    """)
    long countPostsByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
        FROM Post p
        WHERE p.id = :postId AND p.user.id = :userId AND p.active = true
    """)
    boolean isPostOwnedByUser(@Param("postId") Long postId, @Param("userId") Long userId);


}
