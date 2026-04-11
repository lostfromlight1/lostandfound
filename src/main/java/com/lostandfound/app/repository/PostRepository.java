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

}
