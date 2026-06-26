package com.lostandfound.app.service;

import com.lostandfound.app.dto.request.CategoryRequest;
import com.lostandfound.app.dto.response.CategoryResponse;
import java.util.List;

public interface CategoryService {

  CategoryResponse createCategory(CategoryRequest request);

  List<CategoryResponse> getAllCategory();

  public void deleteCategory(Long id);

  CategoryResponse editCategory(Long id, CategoryRequest request);
}
