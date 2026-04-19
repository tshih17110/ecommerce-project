package com.github.ecommerce_project.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private CartMapper cartMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    private Cart cart;
    private CartItem cartItem;
    private CartItemRequestDto request;
    private Product product;

    @BeforeEach
    void setUp() {

        product = Product.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("25.00"))
                .stockQuantity(10)
                .build();

        cartItem = CartItem.builder()
                .product(product)
                .quantity(2)
                .build();

        cart = Cart.builder()
                .id(1L)
                .items(new ArrayList<>(List.of(cartItem)))
                .build();

        request = CartItemRequestDto.builder()
                .productId(1L)
                .quantity(2)
                .build();

    }

    @Nested
    @DisplayName("addItemToCart")
    class AddItemToCart {

        @Test
        @DisplayName("Creates new cart, then adds item")
        void addItemToCart_shouldCreateCartAddItem_whenCalled() {

            User user = User.builder().id(1L).build();
            CartResponseDto expectedDto = new CartResponseDto();

            when(cartRepository.findByUserId(1L))
                    .thenReturn(Optional.empty())
                    .thenReturn(Optional.of(cart));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(cartRepository.save(any(Cart.class))).thenReturn(cart);
            when(cartMapper.toDto(any(Cart.class))).thenReturn(expectedDto);

            cartService.addItemToCart(1L, request);

            verify(cartRepository, times(2)).save(any(Cart.class));
            verify(productRepository).findById(1L);
        }

        @Test
        @DisplayName("Add item to existing cart when product not already in cart")
        void addItemToCart_shouldAddItemToCart_whenProductNotAlreadyInCart() {
            CartResponseDto expectedDto = new CartResponseDto();

            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(cartRepository.save(any(Cart.class))).thenReturn(cart);
            when(cartMapper.toDto(any(Cart.class))).thenReturn(expectedDto);

            cartService.addItemToCart(1L, request);

            verify(cartRepository).save(any(Cart.class));
            verify(productRepository).findById(1L);
        }

        @Test
        @DisplayName("Increments quantity when product already exists in cart")
        void addItemToCart_shouldIncrementProductInCart_whenProductAlreadyExistsInCart() {
            CartResponseDto expectedDto = new CartResponseDto();

            when(cartRepository.findByUserId(1L))
                    .thenReturn(Optional.of(cart))
                    .thenReturn(Optional.of(cart));
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(cartRepository.save(any(Cart.class))).thenReturn(cart);
            when(cartMapper.toDto(any(Cart.class))).thenReturn(expectedDto);

            cartService.addItemToCart(1L, request);

            assertEquals(4, cart.getItems().get(0).getQuantity());
            verify(cartRepository).save(cart);
        }

        @Test
        @DisplayName("Throws when quantity exceeds stock (validateStock)")
        void addItemToCart_shouldThrow_whenQuantityExceedsStock() {
            CartItemRequestDto exceedingRequest = CartItemRequestDto.builder()
                    .productId(1L)
                    .quantity(9)
                    .build();

            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));

            assertThrows(IllegalArgumentException.class, () -> cartService.addItemToCart(1L, exceedingRequest));

            verify(cartRepository, never()).save(any());
        }

        @Test
        @DisplayName("Throws when product not found")
        void addItemToCart_shouldThrow_whenProductNotFound() {
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
            when(productRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(DataNotFoundException.class, () -> cartService.addItemToCart(1L, request));

            verify(cartRepository, never()).save(any());
        }

    }

    @Nested
    @DisplayName("removeItemFromCart")
    class RemoveItemFromCart {

        @Test
        @DisplayName("Throws when cart not found")
        void removeItemFromCart_shouldThrow_whenCartNotFound() {
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());

            assertThrows(DataNotFoundException.class, () -> cartService.removeItemFromCart(1L, request));
        }

        @Test
        @DisplayName("Throws when product not in cart")
        void removeItemFromCart_shouldThrow_whenProductNotInCart() {
            CartItemRequestDto invalidProductRequest = CartItemRequestDto.builder()
                    .productId(99L)
                    .quantity(2)
                    .build();
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

            assertThrows(DataNotFoundException.class, () -> cartService.removeItemFromCart(1L, invalidProductRequest));
            verify(cartRepository, never()).save(any());
        }

        @Test
        @DisplayName("Removes item and returns updated cart")
        void removeItemFromCart_shouldReturnCart_whenItemRemovedFromCart() {
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
            when(cartRepository.save(any(Cart.class))).thenReturn(cart);

            // removeItemFromCart calls getCartByUserId, which calls cartMapper.toDto
            when(cartMapper.toDto(any(Cart.class))).thenReturn(new CartResponseDto());
            cartService.removeItemFromCart(1L, request);

            assertEquals(0, cart.getItems().size());
            verify(cartRepository).save(cart);
        }
    }

    @Nested
    @DisplayName("updateItemQuantity")
    class UpdateItemQuantity {

        @Test
        @DisplayName("Throws when cart not found")
        void updateItemQuantity_shouldThrow_whenCartNotFound() {
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());

            assertThrows(DataNotFoundException.class, () -> cartService.updateItemQuantity(1L, request));
        }

        @Test
        @DisplayName("Throws when product not in cart")
        void updateItemQuantity_shouldThrow_whenProductNotInCart() {
            CartItemRequestDto invalidRequest = CartItemRequestDto.builder()
                    .productId(99L)
                    .quantity(2)
                    .build();
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

            assertThrows(DataNotFoundException.class, () -> cartService.updateItemQuantity(1L, invalidRequest));
            verify(cartRepository, never()).save(any());
        }

        @Test
        @DisplayName("Calls removeItemFromCart when quantity is <= 0")
        void updateItemQuantity_shouldCallRemoveItemFromCart_whenQuantityIsLessThanZero() {
            CartItemRequestDto negativeRequest = CartItemRequestDto.builder()
                    .productId(1L)
                    .quantity(-1)
                    .build();

            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
            when(cartRepository.save(any(Cart.class))).thenReturn(cart);
            when(cartMapper.toDto(any(Cart.class))).thenReturn(new CartResponseDto());

            cartService.updateItemQuantity(1L, negativeRequest);

            assertEquals(0, cart.getItems().size());
            verify(cartRepository).save(cart);
        }

    }

    @Nested
    @DisplayName("clearCart")
    class ClearCart {

        @Test
        @DisplayName("Throws when cart not found")
        void clearCart_shouldThrow_whenCartNotFound() {
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());

            assertThrows(DataNotFoundException.class, () -> cartService.clearCart(1L));
        }

        @Test
        @DisplayName("Calls `deleteAllByCartId` with valid cart")
        void clearCart_shouldClearCart_whenGivenValidCart() {
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

            cartService.clearCart(1L);

            verify(cartItemRepository).deleteAllByCartId(cart.getId());
        }
    }

    @Nested
    @DisplayName("getCartByUserId")
    class getCartByUserId {

        @Test
        @DisplayName("Throws when cart not found")
        void getCartByUserId_shouldThrow_whenCartNotFound() {
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());

            assertThrows(DataNotFoundException.class, () -> cartService.getCartByUserId(1L));
        }

        @Test
        @DisplayName("Returns DTO with calculated `totalCartPrice` and `totalItemCount`")
        void getCartByUserId_returnDto_whenCalledWithValidId() {
            CartResponseDto expectedDto = new CartResponseDto();

            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
            when(cartMapper.toDto(cart)).thenReturn(expectedDto);

            CartResponseDto result = cartService.getCartByUserId(1L);

            assertEquals(new BigDecimal("50.00"), result.getTotalCartPrice());
            assertEquals(2, result.getTotalItemCount());
        }

        @Test
        @DisplayName("Returns zero total for empty cart")
        void getCartByUserId_returnZeroTotal_whenEmptyCart() {
            CartResponseDto expectedDto = new CartResponseDto();
            Cart emptyCart = Cart.builder()
                    .id(1L)
                    .items(new ArrayList<>())
                    .build();

            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(emptyCart));
            when(cartMapper.toDto(emptyCart)).thenReturn(expectedDto);

            CartResponseDto result = cartService.getCartByUserId(1L);

            assertEquals(BigDecimal.ZERO, result.getTotalCartPrice());
            assertEquals(0, result.getTotalItemCount());
        }
    }

}
