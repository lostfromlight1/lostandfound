package com.lostandfound.app.repository;

import com.lostandfound.app.model.Post;
import com.lostandfound.app.model.PostBookmark;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostBookmarkRepository extends JpaRepository<PostBookmark, Long> {

  boolean existsByUserIdAndPostId(Long userId, Long postId);

  Optional<PostBookmark> findByUserIdAndPostId(Long userId, Long postId);

  @Query(
      """
          SELECT pb.post FROM PostBookmark pb
          WHERE pb.user.id = :userId AND pb.post.active = true AND pb.active = true
          ORDER BY pb.createdAt DESC
      """)
  Page<Post> findBookmarkedPostsByUserId(@Param("userId") Long userId, Pageable pageable);
}
