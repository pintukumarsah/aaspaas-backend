package com.aaspaas.aaspaas_backend.cart.service;

import com.aaspaas.aaspaas_backend.cart.dto.AddCartItemRequest;
import com.aaspaas.aaspaas_backend.cart.dto.CartResponse;
import com.aaspaas.aaspaas_backend.cart.dto.UpdateCartItemRequest;

public interface CartService {

    CartResponse getMyCart(String authenticatedPhone);

    CartResponse addItem(
            AddCartItemRequest request,
            String authenticatedPhone
    );

    CartResponse updateItem(
            Long itemId,
            UpdateCartItemRequest request,
            String authenticatedPhone
    );

    void removeItem(
            Long itemId,
            String authenticatedPhone
    );

    void clearCart(String authenticatedPhone);
}