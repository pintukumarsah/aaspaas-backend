package com.aaspaas.aaspaas_backend.cart.repository;

import com.aaspaas.aaspaas_backend.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository
        extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndProductId(
            Long cartId,
            Long productId
    );

    Optional<CartItem> findByIdAndCartId(
            Long itemId,
            Long cartId
    );
}