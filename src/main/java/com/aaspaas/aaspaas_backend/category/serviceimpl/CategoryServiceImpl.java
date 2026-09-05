package com.aaspaas.aaspaas_backend.category.serviceimpl;

import com.aaspaas.aaspaas_backend.category.dto.CategoryResponse;
import com.aaspaas.aaspaas_backend.category.dto.CreateCategoryRequest;
import com.aaspaas.aaspaas_backend.category.entity.Category;
import com.aaspaas.aaspaas_backend.category.repository.CategoryRepository;
import com.aaspaas.aaspaas_backend.category.service.CategoryService;
import com.aaspaas.aaspaas_backend.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl
        implements CategoryService {

    private final CategoryRepository categoryRepository;

  @Override
@Transactional
public CategoryResponse createCategory(
        CreateCategoryRequest request
) {

    if (categoryRepository.existsBySlug(request.getSlug())) {

        throw new BusinessException(
                "Category slug already exists",
                409
        );
    }

    Category category = new Category();

    if (request.getParentId() != null) {

        Category parent = categoryRepository
                .findById(request.getParentId())
                .orElseThrow(() ->
                        new BusinessException(
                                "Parent category not found",
                                404
                        )
                );

        if (!"ACTIVE".equals(parent.getStatus())) {

            throw new BusinessException(
                    "Parent category is inactive",
                    400
            );
        }

        category.setParent(parent);
    }

    category.setName(request.getName());
    category.setSlug(request.getSlug());
    category.setDescription(request.getDescription());
    category.setType(request.getType());
    category.setStatus("ACTIVE");

    Category savedCategory =
            categoryRepository.save(category);

    return CategoryResponse.fromEntity(savedCategory);
}
    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getRootCategories() {

        return categoryRepository
                .findByParentIsNullAndStatus("ACTIVE")
                .stream()
                .map(CategoryResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getChildCategories(
            Long parentId
    ) {

        return categoryRepository
                .findByParentIdAndStatus(
                        parentId,
                        "ACTIVE"
                )
                .stream()
                .map(CategoryResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(
            Long categoryId
    ) {

        Category category =
                categoryRepository.findById(categoryId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Category not found",
                                        404
                                )
                        );

        return CategoryResponse.fromEntity(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllActiveCategories() {

        return categoryRepository
                .findByStatus("ACTIVE")
                .stream()
                .map(CategoryResponse::fromEntity)
                .toList();
    }
}