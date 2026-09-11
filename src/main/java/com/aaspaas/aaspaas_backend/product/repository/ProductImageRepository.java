package com.aaspaas.aaspaas_backend.product.repository;

import com.aaspaas.aaspaas_backend.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductIdOrderByDisplayOrderAsc(Long productId);

    Optional<ProductImage> findByIdAndProductId(Long imageId, Long productId);

    long countByProductId(Long productId);
}