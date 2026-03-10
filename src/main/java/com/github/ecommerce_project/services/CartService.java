package com.github.ecommerce_project.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.ecommerce_project.dtos.cart.CartResponseDto;
import com.github.ecommerce_project.dtos.cartItem.CartItemRequestDto;
import com.github.ecommerce_project.exceptions.DataNotFoundException;
import com.github.ecommerce_project.mapper.CartMapper;
import com.github.ecommerce_project.models.Cart;
import com.github.ecommerce_project.models.CartItem;
import com.github.ecommerce_project.models.Product;
import com.github.ecommerce_project.models.User;
import com.github.ecommerce_project.repositories.CartItemRepository;
import com.github.ecommerce_project.repositories.CartRepository;
import com.github.ecommerce_project.repositories.ProductRepository;
import com.github.ecommerce_project.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartMapper cartMapper;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional
    public CartResponseDto addItemToCart(Long userId, CartItemRequestDto request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new DataNotFoundException("Product not found."));

        validateStock(product, request.getQuantity());

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        // Update quantity if item already in cart
        if (existingItem.isPresent()) {
            int newQuantity = existingItem.get().getQuantity() + request.getQuantity();
            validateStock(product, newQuantity);
            existingItem.get().setQuantity(newQuantity);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(newItem);
        }

        cartRepository.save(cart);
        return getCartByUserId(userId);
    }

    // Remove specified product from cart
    @Transactional
    public CartResponseDto removeItemFromCart(Long userId, CartItemRequestDto request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new DataNotFoundException("Cart not found."));

        CartItem itemToRemove = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(request.getProductId()))
                .findFirst()
                .orElseThrow(() -> new DataNotFoundException("Product not found in cart."));

        cart.getItems().remove(itemToRemove);

        cartRepository.save(cart);
        return getCartByUserId(userId);
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new DataNotFoundException("Cart not found."));

        cartItemRepository.deleteAllByCartId(cart.getId());
    }

    @Transactional
    public CartResponseDto updateItemQuantity(Long userId, CartItemRequestDto request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new DataNotFoundException("Cart not found."));

        if (request.getQuantity() <= 0) {
            return removeItemFromCart(userId, request);
        }

        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(request.getProductId()))
                .findFirst()
                .orElseThrow(() -> new DataNotFoundException("Product not found in cart."));

        existingItem.setQuantity(request.getQuantity());

        cartRepository.save(cart);
        return getCartByUserId(userId);
    }

    @Transactional(readOnly = true)
    public CartResponseDto getCartByUserId(Long userId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new DataNotFoundException("Cart not found."));

        CartResponseDto dto = cartMapper.toDto(cart);
        dto.setTotalCartPrice(calculateTotal(cart));
        dto.setTotalItemCount(calculateItemCount(cart));
        return dto;
    }

    private Cart createNewCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found."));

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setItems(new ArrayList<>());

        return cartRepository.save(cart);
    }

    private BigDecimal calculateTotal(Cart cart) {
        return cart.getItems().stream()
                .map(item -> {
                    BigDecimal price = item.getProduct().getPrice();
                    return price.multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Integer calculateItemCount(Cart cart) {
        if (cart == null || cart.getItems() == null) {
            return 0;
        }
        return cart.getItems().stream()
                .map(CartItem::getQuantity)
                .reduce(0, Integer::sum);
    }

    private void validateStock(Product product, int requestedQuantity) {
        if (product.getStockQuantity() < requestedQuantity) {
            throw new IllegalArgumentException(
                    "Insufficient stock for " + product.getName() + ". " + product.getStockQuantity() + " available.");
        }
    }

}
