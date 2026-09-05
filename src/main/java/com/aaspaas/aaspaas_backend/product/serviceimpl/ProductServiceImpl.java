package com.aaspaas.aaspaas_backend.product.serviceimpl;

import com.aaspaas.aaspaas_backend.business.entity.Business;
import com.aaspaas.aaspaas_backend.business.repository.BusinessRepository;
import com.aaspaas.aaspaas_backend.category.entity.Category;
import com.aaspaas.aaspaas_backend.category.repository.CategoryRepository;
import com.aaspaas.aaspaas_backend.common.exception.BusinessException;
import com.aaspaas.aaspaas_backend.product.dto.CreateProductRequest;
import com.aaspaas.aaspaas_backend.product.dto.ProductResponse;
import com.aaspaas.aaspaas_backend.product.entity.Product;
import com.aaspaas.aaspaas_backend.product.repository.ProductRepository;
import com.aaspaas.aaspaas_backend.product.service.ProductService;
import com.aaspaas.aaspaas_backend.user.entity.User;
import com.aaspaas.aaspaas_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl
        implements ProductService {

    private final ProductRepository productRepository;

    private final BusinessRepository businessRepository;

    private final CategoryRepository categoryRepository;

    private final UserRepository userRepository;

    @Override
    @Transactional
    public ProductResponse createProduct(
            CreateProductRequest request,
            String phone
    ) {

        User user = getUser(phone);

        Business business = businessRepository
                .findById(request.getBusinessId())
                .orElseThrow(() ->
                        new BusinessException(
                                "Business not found",
                                404
                        )
                );

        // Security:
        // Seller can only create products
        // for his own business.
        if (!business.getOwner().getId()
                .equals(user.getId())) {

            throw new BusinessException(
                    "You are not the owner of this business",
                    403
            );
        }

        if (!"ACTIVE".equals(business.getStatus())) {

            throw new BusinessException(
                    "Business is not active",
                    400
            );
        }

        if (productRepository.existsBySlug(
                request.getSlug()
        )) {

            throw new BusinessException(
                    "Product slug already exists",
                    409
            );
        }

        Category category = categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() ->
                        new BusinessException(
                                "Category not found",
                                404
                        )
                );

        if (!"ACTIVE".equals(category.getStatus())) {

            throw new BusinessException(
                    "Category is inactive",
                    400
            );
        }

        Product product = new Product();

        product.setBusiness(business);
        product.setCategory(category);

        product.setName(request.getName());
        product.setSlug(request.getSlug());
        product.setDescription(request.getDescription());

        product.setPrice(request.getPrice());
        product.setStockQuantity(
                request.getStockQuantity()
        );

        product.setUnit(request.getUnit());

        product.setStatus("ACTIVE");

        product.setAvailable(
                request.getAvailable() == null
                        || request.getAvailable()
        );

        Product savedProduct =
                productRepository.save(product);

        return ProductResponse.fromEntity(
                savedProduct
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getMyProducts(
            String phone
    ) {

        User user = getUser(phone);

        return businessRepository
                .findByOwnerId(user.getId())
                .stream()
                .flatMap(business ->
                        productRepository
                                .findByBusinessId(
                                        business.getId()
                                )
                                .stream()
                )
                .map(ProductResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getBusinessProducts(
            Long businessId
    ) {

        return productRepository
                .findByBusinessIdAndStatus(
                        businessId,
                        "ACTIVE"
                )
                .stream()
                .map(ProductResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(
            Long productId
    ) {

        Product product =
                productRepository.findById(productId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Product not found",
                                        404
                                )
                        );

        return ProductResponse.fromEntity(product);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(
            Long productId,
            CreateProductRequest request,
            String phone
    ) {

        User user = getUser(phone);

        Product product =
                productRepository.findById(productId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Product not found",
                                        404
                                )
                        );

        if (!product.getBusiness()
                .getOwner()
                .getId()
                .equals(user.getId())) {

            throw new BusinessException(
                    "You are not the owner of this product",
                    403
            );
        }

        Category category =
                categoryRepository.findById(
                        request.getCategoryId()
                ).orElseThrow(() ->
                        new BusinessException(
                                "Category not found",
                                404
                        )
                );

        if (!"ACTIVE".equals(category.getStatus())) {

            throw new BusinessException(
                    "Category is inactive",
                    400
            );
        }

        product.setCategory(category);
        product.setName(request.getName());
        product.setSlug(request.getSlug());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(
                request.getStockQuantity()
        );
        product.setUnit(request.getUnit());

        if (request.getAvailable() != null) {
            product.setAvailable(
                    request.getAvailable()
            );
        }

        Product updatedProduct =
                productRepository.save(product);

        return ProductResponse.fromEntity(
                updatedProduct
        );
    }

    @Override
    @Transactional
    public void deleteProduct(
            Long productId,
            String phone
    ) {

        User user = getUser(phone);

        Product product =
                productRepository.findById(productId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Product not found",
                                        404
                                )
                        );

        if (!product.getBusiness()
                .getOwner()
                .getId()
                .equals(user.getId())) {

            throw new BusinessException(
                    "You are not the owner of this product",
                    403
            );
        }

        // Soft delete
        product.setStatus("INACTIVE");
        product.setAvailable(false);

        productRepository.save(product);
    }

    private User getUser(String phone) {

        return userRepository
                .findByPhone(phone)
                .orElseThrow(() ->
                        new BusinessException(
                                "User not found",
                                404
                        )
                );
    }
}