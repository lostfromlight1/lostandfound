package com.lostandfound.app.repository;

import com.lostandfound.app.model.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, Long> {

    @Query("""
        SELECT r FROM Reply r
        JOIN FETCH r.user u
        WHERE r.comment.id = :commentId AND r.active = true
        ORDER BY r.createdAt ASC
    """)
    List<Reply> findActiveRepliesByCommentId(@Param("commentId") Long commentId);

    @Query("""
        SELECT r FROM Reply r
        JOIN FETCH r.user u
        WHERE r.comment.id = :commentId AND r.replyTo IS NULL AND r.active = true
        ORDER BY r.createdAt ASC
    """)
    List<Reply> findActiveRootRepliesByCommentId(@Param("commentId") Long commentId);

    @Query("""
        SELECT r FROM Reply r
        JOIN FETCH r.user u
        WHERE r.replyTo.id = :replyId AND r.active = true
        ORDER BY r.createdAt ASC
    """)
    List<Reply> findActiveNestedRepliesByReplyId(@Param("replyId") Long replyId);

    void deleteByCommentId(Long commentId);
}
