package com.lostandfound.app.util;

import com.lostandfound.app.model.MyanmarCity;
import com.lostandfound.app.model.Post;
import com.lostandfound.app.model.PostType;
import org.springframework.data.jpa.domain.Specification;

public class PostSpecification {

    public static Specification<Post> hasType(PostType type) {
        return (root, query, cb) ->
                type == null ? null : cb.equal(root.get("postType"),type);
    }

    public static Specification<Post> hasCategory(Long categoryId) {
        return (root, query, cb) ->
                categoryId == null ? null : cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Post> hasLocation(MyanmarCity location) {
        return (root, query, cb) ->
                location == null ? null : cb.equal(root.get("location"), location);
    }
}