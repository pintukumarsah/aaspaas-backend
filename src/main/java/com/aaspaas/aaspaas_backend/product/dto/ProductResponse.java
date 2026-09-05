package com.aaspaas.aaspaas_backend.product.dto;

import com.aaspaas.aaspaas_backend.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ProductResponse {

    private Long id;

    private Long businessId;

    private String businessName;

    private Long categoryId;

    private String categoryName;

    private String name;

    private String slug;

    private String description;

    private BigDecimal price;

    private BigDecimal stockQuantity;

    private String unit;

    private String status;

    private Boolean available;

    public static ProductResponse fromEntity(
            Product product
    ) {

        return new ProductResponse(
                product.getId(),
                product.getBusiness().getId(),
                product.getBusiness().getName(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getUnit(),
                product.getStatus(),
                product.getAvailable()
        );
    }
}