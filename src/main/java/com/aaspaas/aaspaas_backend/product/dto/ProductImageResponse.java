package com.aaspaas.aaspaas_backend.product.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class ProductImageResponse {

    private Long id;

    private Long productId;

    private String imageUrl;

    private Integer displayOrder;

    private OffsetDateTime createdAt;
}