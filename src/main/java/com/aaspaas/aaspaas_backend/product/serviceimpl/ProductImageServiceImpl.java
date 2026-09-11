package com.aaspaas.aaspaas_backend.product.serviceimpl;

import com.aaspaas.aaspaas_backend.business.entity.Business;
import com.aaspaas.aaspaas_backend.common.exception.BusinessException;
import com.aaspaas.aaspaas_backend.product.dto.AddProductImageRequest;
import com.aaspaas.aaspaas_backend.product.dto.ProductImageResponse;
import com.aaspaas.aaspaas_backend.product.entity.Product;
import com.aaspaas.aaspaas_backend.product.entity.ProductImage;
import com.aaspaas.aaspaas_backend.product.repository.ProductImageRepository;
import com.aaspaas.aaspaas_backend.product.repository.ProductRepository;
import com.aaspaas.aaspaas_backend.product.service.ProductImageService;
import com.aaspaas.aaspaas_backend.user.entity.User;
import com.aaspaas.aaspaas_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductRepository productRepository;

    private final ProductImageRepository productImageRepository;

    private final UserRepository userRepository;


    @Override
    public ProductImageResponse addImage(
            Long productId,
            AddProductImageRequest request,
            String authenticatedPhone
    ) {

        User user = userRepository.findByPhone(authenticatedPhone)
                .orElseThrow(() ->
                        new BusinessException(
                                "User not found",
                                404
                        )
                );

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new BusinessException(
                                "Product not found",
                                404
                        )
                );

        validateProductOwner(product, user);

        if (!"ACTIVE".equalsIgnoreCase(product.getStatus())) {

            throw new BusinessException(
                    "Cannot add image to inactive product",
                    400
            );
        }

        long imageCount =
                productImageRepository.countByProductId(productId);

        if (imageCount >= 10) {

            throw new BusinessException(
                    "Maximum 10 images allowed for one product",
                    400
            );
        }

        ProductImage image = new ProductImage();

        image.setProduct(product);

        image.setImageUrl(
                request.getImageUrl()
        );

        image.setDisplayOrder(
                request.getDisplayOrder() != null
                        ? request.getDisplayOrder()
                        : 0
        );

        ProductImage savedImage =
                productImageRepository.save(image);

        return mapToResponse(savedImage);
    }


    @Override
    @Transactional(readOnly = true)
    public List<ProductImageResponse> getProductImages(
            Long productId
    ) {

        if (!productRepository.existsById(productId)) {

            throw new BusinessException(
                    "Product not found",
                    404
            );
        }

        return productImageRepository
                .findByProductIdOrderByDisplayOrderAsc(productId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    @Override
    public void deleteImage(
            Long productId,
            Long imageId,
            String authenticatedPhone
    ) {

        User user = userRepository.findByPhone(authenticatedPhone)
                .orElseThrow(() ->
                        new BusinessException(
                                "User not found",
                                404
                        )
                );

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new BusinessException(
                                "Product not found",
                                404
                        )
                );

        validateProductOwner(product, user);

        ProductImage image =
                productImageRepository
                        .findByIdAndProductId(
                                imageId,
                                productId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Product image not found",
                                        404
                                )
                        );

        productImageRepository.delete(image);
    }


    private void validateProductOwner(
            Product product,
            User user
    ) {

        Business business = product.getBusiness();

        if (business == null ||
                business.getOwner() == null ||
                !business.getOwner()
                        .getId()
                        .equals(user.getId())) {

            throw new BusinessException(
                    "You are not the owner of this product",
                    403
            );
        }
    }


    private ProductImageResponse mapToResponse(
            ProductImage image
    ) {

        ProductImageResponse response =
                new ProductImageResponse();

        response.setId(image.getId());

        response.setProductId(
                image.getProduct().getId()
        );

        response.setImageUrl(
                image.getImageUrl()
        );

        response.setDisplayOrder(
                image.getDisplayOrder()
        );

        response.setCreatedAt(
                image.getCreatedAt()
        );

        return response;
    }
}