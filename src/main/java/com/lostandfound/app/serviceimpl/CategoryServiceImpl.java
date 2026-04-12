package com.lostandfound.app.serviceimpl;

import com.lostandfound.app.dto.request.CategoryRequest;
import com.lostandfound.app.dto.response.CategoryResponse;
import com.lostandfound.app.model.Category;
import com.lostandfound.app.repository.CategoryRepository;
import com.lostandfound.app.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {

        categoryRepository.findByNameIgnoreCase(request.name())
                .ifPresent(c -> {
                    throw new RuntimeException("Category already exists");
                });

        Category category = new Category();
        category.setName(request.name());

        categoryRepository.save(category);

        return new CategoryResponse(category.getId(), category.getName());

    }

    @Override
    public List<CategoryResponse> getAllCategory() {
        return categoryRepository.findAll().stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName())).toList();
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("ID NOT Found"));

        categoryRepository.delete(category);
    }

    @Override
    public CategoryResponse editCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("ID NOT Found"));

        category.setName((request.name()));

        categoryRepository.save(category);

        return new CategoryResponse(category.getId(),category.getName());

    }
}
