package com.aaspaas.aaspaas_backend.cart.serviceimpl;

import com.aaspaas.aaspaas_backend.cart.dto.AddCartItemRequest;
import com.aaspaas.aaspaas_backend.cart.dto.CartItemResponse;
import com.aaspaas.aaspaas_backend.cart.dto.CartResponse;
import com.aaspaas.aaspaas_backend.cart.dto.UpdateCartItemRequest;
import com.aaspaas.aaspaas_backend.cart.entity.Cart;
import com.aaspaas.aaspaas_backend.cart.entity.CartItem;
import com.aaspaas.aaspaas_backend.cart.repository.CartItemRepository;
import com.aaspaas.aaspaas_backend.cart.repository.CartRepository;
import com.aaspaas.aaspaas_backend.common.exception.BusinessException;
import com.aaspaas.aaspaas_backend.product.entity.Product;
import com.aaspaas.aaspaas_backend.product.repository.ProductRepository;
import com.aaspaas.aaspaas_backend.user.entity.User;
import com.aaspaas.aaspaas_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.aaspaas.aaspaas_backend.cart.service.CartService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getMyCart(
            String authenticatedPhone
    ) {

        User user = getUser(authenticatedPhone);

        Cart cart = cartRepository
                .findByUserId(user.getId())
                .orElseGet(() -> createCart(user));

        return mapToResponse(cart);
    }

    @Override
    public CartResponse addItem(
            AddCartItemRequest request,
            String authenticatedPhone
    ) {

        User user = getUser(authenticatedPhone);

        Product product = productRepository
                .findById(request.getProductId())
                .orElseThrow(() ->
                        new BusinessException(
                                "Product not found",
                                404
                        )
                );

        validateProduct(product);

        validateStock(
                product,
                request.getQuantity()
        );

        Cart cart = cartRepository
                .findByUserId(user.getId())
                .orElseGet(() -> createCart(user));

        /*
         * A cart currently belongs to one business.
         *
         * This keeps checkout simple and prevents
         * one cart from containing products from
         * multiple sellers.
         */
        validateBusinessRule(cart, product);

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(
                        cart.getId(),
                        product.getId()
                )
                .orElse(null);

        if (item == null) {

            item = new CartItem();

            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(request.getQuantity());

        } else {

            BigDecimal newQuantity =
                    item.getQuantity()
                            .add(request.getQuantity());

            validateStock(product, newQuantity);

            item.setQuantity(newQuantity);
        }

        cartItemRepository.save(item);

        return mapToResponse(cart);
    }

    @Override
    public CartResponse updateItem(
            Long itemId,
            UpdateCartItemRequest request,
            String authenticatedPhone
    ) {

        User user = getUser(authenticatedPhone);

        Cart cart = getCart(user);

        CartItem item = cartItemRepository
                .findByIdAndCartId(
                        itemId,
                        cart.getId()
                )
                .orElseThrow(() ->
                        new BusinessException(
                                "Cart item not found",
                                404
                        )
                );

        Product product = item.getProduct();

        validateProduct(product);

        validateStock(
                product,
                request.getQuantity()
        );

        item.setQuantity(request.getQuantity());

        cartItemRepository.save(item);

        return mapToResponse(cart);
    }

    @Override
    public void removeItem(
            Long itemId,
            String authenticatedPhone
    ) {

        User user = getUser(authenticatedPhone);

        Cart cart = getCart(user);

        CartItem item = cartItemRepository
                .findByIdAndCartId(
                        itemId,
                        cart.getId()
                )
                .orElseThrow(() ->
                        new BusinessException(
                                "Cart item not found",
                                404
                        )
                );

        cartItemRepository.delete(item);
    }

    @Override
    public void clearCart(
            String authenticatedPhone
    ) {

        User user = getUser(authenticatedPhone);

        Cart cart = getCart(user);

        cart.getItems().clear();

        cartRepository.save(cart);
    }

    private User getUser(
            String authenticatedPhone
    ) {

        return userRepository
                .findByPhone(authenticatedPhone)
                .orElseThrow(() ->
                        new BusinessException(
                                "User not found",
                                404
                        )
                );
    }

    private Cart getCart(User user) {

        return cartRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new BusinessException(
                                "Cart not found",
                                404
                        )
                );
    }

    private Cart createCart(User user) {

        Cart cart = new Cart();

        cart.setUser(user);

        return cartRepository.save(cart);
    }

    private void validateProduct(
            Product product
    ) {

        if (!"ACTIVE".equalsIgnoreCase(
                product.getStatus()
        )) {

            throw new BusinessException(
                    "Product is not active",
                    400
            );
        }

        if (!Boolean.TRUE.equals(
                product.getAvailable()
        )) {

            throw new BusinessException(
                    "Product is currently unavailable",
                    400
            );
        }
    }

    private void validateStock(
            Product product,
            BigDecimal quantity
    ) {

        if (quantity.compareTo(
                product.getStockQuantity()
        ) > 0) {

            throw new BusinessException(
                    "Requested quantity exceeds available stock",
                    400
            );
        }
    }

    private void validateBusinessRule(
            Cart cart,
            Product product
    ) {

        if (cart.getItems() == null ||
                cart.getItems().isEmpty()) {

            return;
        }

        Long existingBusinessId =
                cart.getItems()
                        .get(0)
                        .getProduct()
                        .getBusiness()
                        .getId();

        Long newBusinessId =
                product.getBusiness().getId();

        if (!existingBusinessId.equals(newBusinessId)) {

            throw new BusinessException(
                    "Cart can contain products from only one business",
                    400
            );
        }
    }

    private CartResponse mapToResponse(
            Cart cart
    ) {

        CartResponse response = new CartResponse();

        response.setCartId(cart.getId());

        response.setUserId(
                cart.getUser().getId()
        );

        if (cart.getItems() == null) {

            response.setItems(
                    new ArrayList<>()
            );

            response.setSubtotal(
                    BigDecimal.ZERO
            );

            response.setTotalItems(0);

            return response;
        }

        var itemResponses =
                cart.getItems()
                        .stream()
                        .map(this::mapItem)
                        .toList();

        BigDecimal subtotal =
                itemResponses.stream()
                        .map(CartItemResponse::getTotalPrice)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        int totalItems =
                cart.getItems()
                        .stream()
                        .map(CartItem::getQuantity)
                        .mapToInt(BigDecimal::intValue)
                        .sum();

        response.setItems(itemResponses);
        response.setSubtotal(subtotal);
        response.setTotalItems(totalItems);

        return response;
    }

    private CartItemResponse mapItem(
            CartItem item
    ) {

        Product product = item.getProduct();

        CartItemResponse response =
                new CartItemResponse();

        response.setId(item.getId());

        response.setProductId(
                product.getId()
        );

        response.setProductName(
                product.getName()
        );

        response.setUnitPrice(
                product.getPrice()
        );

        response.setQuantity(
                item.getQuantity()
        );

        response.setTotalPrice(
                product.getPrice()
                        .multiply(item.getQuantity())
        );

        response.setBusinessId(
                product.getBusiness().getId()
        );

        response.setBusinessName(
                product.getBusiness().getName()
        );

        return response;
    }
}