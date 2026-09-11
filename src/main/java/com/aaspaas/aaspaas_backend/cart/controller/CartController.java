package com.aaspaas.aaspaas_backend.cart.controller;

import com.aaspaas.aaspaas_backend.cart.dto.AddCartItemRequest;
import com.aaspaas.aaspaas_backend.cart.dto.CartResponse;
import com.aaspaas.aaspaas_backend.cart.dto.UpdateCartItemRequest;
import com.aaspaas.aaspaas_backend.cart.service.CartService;
import com.aaspaas.aaspaas_backend.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getMyCart(
            Principal principal
    ) {

        CartResponse response =
                cartService.getMyCart(
                        principal.getName()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cart fetched successfully",
                        response
                )
        );
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @Valid @RequestBody AddCartItemRequest request,
            Principal principal
    ) {

        CartResponse response =
                cartService.addItem(
                        request,
                        principal.getName()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Product added to cart successfully",
                        response
                )
        );
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request,
            Principal principal
    ) {

        CartResponse response =
                cartService.updateItem(
                        itemId,
                        request,
                        principal.getName()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cart item updated successfully",
                        response
                )
        );
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> removeItem(
            @PathVariable Long itemId,
            Principal principal
    ) {

        cartService.removeItem(
                itemId,
                principal.getName()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cart item removed successfully",
                        null
                )
        );
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(
            Principal principal
    ) {

        cartService.clearCart(
                principal.getName()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cart cleared successfully",
                        null
                )
        );
    }
}