package com.lostandfound.app.util;

import com.lostandfound.app.model.MyanmarCity;
import com.lostandfound.app.model.Post;
import com.lostandfound.app.model.PostType;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;

public class PostSpecification {

    public static Specification<Post> hasType(PostType type) {
        return (root, query, cb) ->
                type == null ? null : cb.equal(root.get("postType"), type);
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
            return cb.like(cb.lower(root.get("locationDetails")), "%" + details.toLowerCase() + "%");
        };
    }

    public static Specification<Post> hasLostFoundBetween(LocalDate start, LocalDate end) {
        return (root, query, cb) -> {
            if (start == null && end == null) return null;

            if (start != null && end != null) {
                return cb.between(root.get("lostFoundDate"), start, end);
            }

            if (start != null) {
                return cb.greaterThanOrEqualTo(root.get("lostFoundDate"), start);
            }

            return cb.lessThanOrEqualTo(root.get("lostFoundDate"), end);
        };
    }
}