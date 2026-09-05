package com.aaspaas.aaspaas_backend.category.dto;

import com.aaspaas.aaspaas_backend.category.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryResponse {

    private Long id;

    private Long parentId;

    private String name;

    private String slug;

    private String description;

    private String type;

    private String status;

    public static CategoryResponse fromEntity(Category category) {

        Long parentId = null;

        if (category.getParent() != null) {
            parentId = category.getParent().getId();
        }

        return new CategoryResponse(
                category.getId(),
                parentId,
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getType(),
                category.getStatus()
        );
    }
}