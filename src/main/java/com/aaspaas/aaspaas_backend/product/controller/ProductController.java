package com.aaspaas.aaspaas_backend.product.controller;

import com.aaspaas.aaspaas_backend.product.dto.CreateProductRequest;
import com.aaspaas.aaspaas_backend.product.dto.ProductResponse;
import com.aaspaas.aaspaas_backend.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            Authentication authentication
    ) {

        ProductResponse response =
                productService.createProduct(
                        request,
                        authentication.getName()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ProductResponse>>
    getMyProducts(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                productService.getMyProducts(
                        authentication.getName()
                )
        );
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<List<ProductResponse>>
    getBusinessProducts(
            @PathVariable Long businessId
    ) {

        return ResponseEntity.ok(
                productService.getBusinessProducts(
                        businessId
                )
        );
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse>
    getProductById(
            @PathVariable Long productId
    ) {

        return ResponseEntity.ok(
                productService.getProductById(
                        productId
                )
        );
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponse>
    updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody CreateProductRequest request,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                productService.updateProduct(
                        productId,
                        request,
                        authentication.getName()
                )
        );
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long productId,
            Authentication authentication
    ) {

        productService.deleteProduct(
                productId,
                authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }
}