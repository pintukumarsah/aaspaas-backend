package com.aaspaas.aaspaas_backend.product.service;

import com.aaspaas.aaspaas_backend.product.dto.AddProductImageRequest;
import com.aaspaas.aaspaas_backend.product.dto.ProductImageResponse;

import java.util.List;

public interface ProductImageService {

    ProductImageResponse addImage(
            Long productId,
            AddProductImageRequest request,
            String authenticatedPhone
    );

    List<ProductImageResponse> getProductImages(
            Long productId
    );

    void deleteImage(
            Long productId,
            Long imageId,
            String authenticatedPhone
    );
}