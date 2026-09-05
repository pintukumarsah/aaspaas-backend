package com.aaspaas.aaspaas_backend.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCategoryRequest {

    private Long parentId;

    @NotBlank(message = "Category name is required")
    @Size(
            min = 2,
            max = 100,
            message = "Category name must be between 2 and 100 characters"
    )
    private String name;

    @NotBlank(message = "Slug is required")
    @Size(
            min = 2,
            max = 120,
            message = "Slug must be between 2 and 120 characters"
    )
    private String slug;

    @Size(
            max = 1000,
            message = "Description cannot exceed 1000 characters"
    )
    private String description;

    @NotBlank(message = "Category type is required")
    private String type;
}