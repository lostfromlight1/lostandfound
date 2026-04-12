package com.lostandfound.app.repository;

import com.lostandfound.app.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category,Long> {

    Optional<Category> findByNameIgnoreCase(String name);
}
