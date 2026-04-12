package com.lostandfound.app.controller;

import com.lostandfound.app.annotation.ApiId;
import com.lostandfound.app.annotation.CheckSecurity;
import com.lostandfound.app.dto.request.CategoryRequest;
import com.lostandfound.app.dto.response.BaseResponse;
import com.lostandfound.app.dto.response.CategoryResponse;
import com.lostandfound.app.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "6. Category Management", description = "Endpoints for creating and managing item categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @CheckSecurity.Admin.isRequired
    @ApiId("CAT-001")
    @Operation(summary = "Create Category", description = "Creates a new category. Only accessible by admins.")
    public ResponseEntity<BaseResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryRequest request) {

        log.info("REST request to create category: {}", request.name());
        CategoryResponse response = categoryService.createCategory(request);
        return BaseResponse.created("Category created successfully", response);
    }

    @GetMapping
    @CheckSecurity.Public.canRead
    @ApiId("CAT-002")
    @Operation(summary = "Get All Categories", description = "Fetches a list of all active categories.")
    public ResponseEntity<BaseResponse<List<CategoryResponse>>> getAllCategories() {

        log.info("REST request to get all categories");
        List<CategoryResponse> responses = categoryService.getAllCategory();
        return BaseResponse.success("Categories fetched successfully", responses);
    }

    @PutMapping("/{id}")
    @CheckSecurity.Admin.isRequired
    @ApiId("CAT-003")
    @Operation(summary = "Update Category", description = "Updates the name of an existing category. Only accessible by admins.")
    public ResponseEntity<BaseResponse<CategoryResponse>> editCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {

        log.info("REST request to update category ID: {}", id);
        CategoryResponse response = categoryService.editCategory(id, request);
        return BaseResponse.success("Category updated successfully", response);
    }

    @DeleteMapping("/{id}")
    @CheckSecurity.Admin.isRequired
    @ApiId("CAT-004")
    @Operation(summary = "Delete Category", description = "Soft deletes a category. Only accessible by admins.")
    public ResponseEntity<BaseResponse<Void>> deleteCategory(
            @PathVariable Long id) {

        log.info("REST request to delete category ID: {}", id);
        categoryService.deleteCategory(id);
        return BaseResponse.success("Category deleted successfully");
    }
}