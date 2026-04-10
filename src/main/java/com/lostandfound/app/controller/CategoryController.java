package com.lostandfound.app.controller;


import com.lostandfound.app.dto.request.CategoryRequest;
import com.lostandfound.app.dto.response.BaseResponse;
import com.lostandfound.app.dto.response.CategoryResponse;
import com.lostandfound.app.model.Category;
import com.lostandfound.app.annotation.CheckSecurity;
import com.lostandfound.app.service.CategoryService;
import jakarta.validation.Valid;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/category")
@Slf4j
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CategoryController {

    private final CategoryService categoryService;

    @CheckSecurity.Admin.isRequired
    @PostMapping
    public ResponseEntity<BaseResponse<CategoryResponse>> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);

        return BaseResponse.created("create Category Successful", response);
    }

    @GetMapping
    @CheckSecurity.Public.canRead
    public ResponseEntity<BaseResponse<List<CategoryResponse>>> getAllCategory() {
        List<CategoryResponse> responses = categoryService.getAllCategory();

        return BaseResponse.success("Fetching category Success", responses);
    }

    @CheckSecurity.Admin.isRequired
    @PostMapping("/edit/{id}")
    public ResponseEntity<BaseResponse<CategoryResponse>> editCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.editCategory(id, request);

        return BaseResponse.created("create Category Successful", response);
    }

    @CheckSecurity.Admin.isRequired
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);

        return BaseResponse.success("delete Category Successful");


    }


}
