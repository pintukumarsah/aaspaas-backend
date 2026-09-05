package com.aaspaas.aaspaas_backend.product.service;

import com.aaspaas.aaspaas_backend.product.dto.CreateProductRequest;
import com.aaspaas.aaspaas_backend.product.dto.ProductResponse;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(
            CreateProductRequest request,
            String phone
    );

    List<ProductResponse> getMyProducts(
            String phone
    );

    List<ProductResponse> getBusinessProducts(
            Long businessId
    );

    ProductResponse getProductById(
            Long productId
    );

    ProductResponse updateProduct(
            Long productId,
            CreateProductRequest request,
            String phone
    );

    void deleteProduct(
            Long productId,
            String phone
    );
}