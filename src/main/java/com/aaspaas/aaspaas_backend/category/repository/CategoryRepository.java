package com.aaspaas.aaspaas_backend.category.repository;

import com.aaspaas.aaspaas_backend.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository
        extends JpaRepository<Category, Long> {

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Category> findByParentIsNullAndStatus(String status);

    List<Category> findByParentIdAndStatus(
            Long parentId,
            String status
    );

    List<Category> findByStatus(String status);
}