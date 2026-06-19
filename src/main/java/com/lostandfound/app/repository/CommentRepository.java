package com.lostandfound.app.repository;

import com.lostandfound.app.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    long countByPostIdAndDeletedAtIsNull(Long postId);

    @Query("""
        SELECT c FROM Comment c
        JOIN FETCH c.user u
        WHERE c.post.id = :postId AND c.active = true
        ORDER BY c.createdAt DESC
    """)
    List<Comment> findActiveCommentsWithUserByPostId(@Param("postId") Long postId);
}

