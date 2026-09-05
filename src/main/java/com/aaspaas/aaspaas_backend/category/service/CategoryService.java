package com.aaspaas.aaspaas_backend.category.service;

import com.aaspaas.aaspaas_backend.category.dto.CategoryResponse;
import com.aaspaas.aaspaas_backend.category.dto.CreateCategoryRequest;

import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(
            CreateCategoryRequest request
    );

    List<CategoryResponse> getRootCategories();

    List<CategoryResponse> getChildCategories(
            Long parentId
    );

    CategoryResponse getCategoryById(
            Long categoryId
    );

    List<CategoryResponse> getAllActiveCategories();
}