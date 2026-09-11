package com.aaspaas.aaspaas_backend.product.controller;

import com.aaspaas.aaspaas_backend.common.dto.ApiResponse;
import com.aaspaas.aaspaas_backend.product.dto.AddProductImageRequest;
import com.aaspaas.aaspaas_backend.product.dto.ProductImageResponse;
import com.aaspaas.aaspaas_backend.product.service.ProductImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/products/{productId}/images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService productImageService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<?> addImage(
            @PathVariable Long productId,
            @Valid @RequestBody AddProductImageRequest request,
            Principal principal
    ) {

        ProductImageResponse response =
                productImageService.addImage(
                        productId,
                        request,
                        principal.getName()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Product image added successfully",
                                response
                        )
                );
    }

    @GetMapping
    public ResponseEntity<?> getImages(
            @PathVariable Long productId
    ) {

        List<ProductImageResponse> images =
                productImageService.getProductImages(productId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Product images fetched successfully",
                        images
                )
        );
    }

    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<?> deleteImage(
            @PathVariable Long productId,
            @PathVariable Long imageId,
            Principal principal
    ) {

        productImageService.deleteImage(
                productId,
                imageId,
                principal.getName()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Product image deleted successfully",
                        null
                )
        );
    }
}