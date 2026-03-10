package com.github.ecommerce_project.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.ecommerce_project.dtos.cart.CartResponseDto;
import com.github.ecommerce_project.dtos.cartItem.CartItemRequestDto;
import com.github.ecommerce_project.services.CartService;
import com.github.ecommerce_project.utils.SecurityUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponseDto> getMyCart() {
        Long userId = SecurityUtils.getAuthenticatedUserId();
        return ResponseEntity.ok(cartService.getCartByUserId(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponseDto> addItem(@Valid @RequestBody CartItemRequestDto request) {
        Long userId = SecurityUtils.getAuthenticatedUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addItemToCart(userId, request));
    }

    @PutMapping("/items")
    public ResponseEntity<CartResponseDto> updateItemQuantity(@Valid @RequestBody CartItemRequestDto request) {
        Long userId = SecurityUtils.getAuthenticatedUserId();
        return ResponseEntity.ok(cartService.updateItemQuantity(userId, request));
    }

    @DeleteMapping("/items")
    public ResponseEntity<CartResponseDto> removeItem(@Valid @RequestBody CartItemRequestDto request) {
        Long userId = SecurityUtils.getAuthenticatedUserId();
        return ResponseEntity.ok(cartService.removeItemFromCart(userId, request));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        Long userId = SecurityUtils.getAuthenticatedUserId();
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

}
