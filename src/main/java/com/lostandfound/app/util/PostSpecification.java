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

    public static Specification<Post> hasCity(MyanmarCity city) {
        return (root, query, cb) ->
                city == null ? null : cb.equal(root.get("city"), city);
    }

    public static Specification<Post> hasLocationDetails(String details) {
        return (root, query, cb) -> {
            if (details == null || details.isBlank()) return null;
            // This allows a "Like" search (e.g., searching "Junction" finds "Junction City")
            return cb.like(cb.lower(root.get("locationDetails")), "%" + details.toLowerCase() + "%");
        };
    }
}