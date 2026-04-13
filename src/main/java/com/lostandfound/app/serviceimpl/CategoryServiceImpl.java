package com.lostandfound.app.serviceimpl;

import com.lostandfound.app.dto.request.CategoryRequest;
import com.lostandfound.app.dto.response.CategoryResponse;
import com.lostandfound.app.exception.AppException;
import com.lostandfound.app.exception.ErrorCode;
import com.lostandfound.app.model.Category;
import com.lostandfound.app.repository.CategoryRepository;
import com.lostandfound.app.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        log.info("[{}] Attempting to create category: {}", getTraceId(), request.name());

        categoryRepository.findByNameIgnoreCase(request.name())
                .ifPresent(c -> {
                    log.warn("[{}] Category already exists: {}", getTraceId(), request.name());
                    throw new AppException(ErrorCode.RESOURCE_ALREADY_EXISTS, "Category already exists");
                });

        Category category = new Category();
        category.setName(request.name());

        categoryRepository.save(category);
        log.info("[{}] Successfully created category ID: {}", getTraceId(), category.getId());

        return new CategoryResponse(category.getId(), category.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategory() {
        log.info("[{}] Fetching all categories", getTraceId());
        return categoryRepository.findAll().stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName())).toList();
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        log.info("[{}] Attempting to delete category ID: {}", getTraceId(), id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Category not found"));

        categoryRepository.delete(category);
        log.info("[{}] Successfully deleted category ID: {}", getTraceId(), id);
    }

    @Override
    @Transactional
    public CategoryResponse editCategory(Long id, CategoryRequest request) {
        log.info("[{}] Attempting to edit category ID: {}", getTraceId(), id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Category not found"));

        category.setName(request.name());
        categoryRepository.save(category);

        log.info("[{}] Successfully updated category ID: {}", getTraceId(), id);
        return new CategoryResponse(category.getId(), category.getName());
    }

    private String getTraceId() {
        return Objects.toString(MDC.get("traceId"), "SYSTEM");
    }
}