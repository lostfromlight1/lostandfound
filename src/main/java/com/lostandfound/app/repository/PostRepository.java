package com.lostandfound.app.repository;

import com.lostandfound.app.model.Post;
import com.lostandfound.app.model.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post,Long> {
    @Query(
            value = "SELECT p FROM Post p " +
                    "JOIN FETCH p.user " +
                    "JOIN FETCH p.category " + // Add this line!
                    "WHERE p.postType = :type",
            countQuery = "SELECT count(p) FROM Post p WHERE p.postType = :type"
    )
    Page<Post> findByPostTypeWithUserAndCategory(@Param("type") PostType type, Pageable pageable);
}
