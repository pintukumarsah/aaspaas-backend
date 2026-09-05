package com.aaspaas.aaspaas_backend.category.controller;

import com.aaspaas.aaspaas_backend.category.dto.CategoryResponse;
import com.aaspaas.aaspaas_backend.category.dto.CreateCategoryRequest;
import com.aaspaas.aaspaas_backend.category.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CreateCategoryRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        categoryService.createCategory(request)
                );
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>>
    getAllActiveCategories() {

        return ResponseEntity.ok(
                categoryService.getAllActiveCategories()
        );
    }

    @GetMapping("/root")
    public ResponseEntity<List<CategoryResponse>>
    getRootCategories() {

        return ResponseEntity.ok(
                categoryService.getRootCategories()
        );
    }

    @GetMapping("/{categoryId}/children")
    public ResponseEntity<List<CategoryResponse>>
    getChildCategories(
            @PathVariable Long categoryId
    ) {

        return ResponseEntity.ok(
                categoryService.getChildCategories(
                        categoryId
                )
        );
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse>
    getCategoryById(
            @PathVariable Long categoryId
    ) {

        return ResponseEntity.ok(
                categoryService.getCategoryById(
                        categoryId
                )
        );
    }
}