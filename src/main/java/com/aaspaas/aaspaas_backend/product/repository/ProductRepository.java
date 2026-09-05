package com.aaspaas.aaspaas_backend.product.repository;

import com.aaspaas.aaspaas_backend.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository
        extends JpaRepository<Product, Long> {

    boolean existsBySlug(String slug);

    Optional<Product> findBySlug(String slug);

    List<Product> findByBusinessId(Long businessId);

    List<Product> findByBusinessIdAndStatus(
            Long businessId,
            String status
    );

    List<Product> findByCategoryIdAndStatus(
            Long categoryId,
            String status
    );

    List<Product> findByAvailableTrueAndStatus(
            String status
    );
}