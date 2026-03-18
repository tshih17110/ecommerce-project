package com.github.ecommerce_project.services;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.ecommerce_project.dtos.order.OrderRequestDto;
import com.github.ecommerce_project.dtos.orderItem.OrderItemRequestDto;
import com.github.ecommerce_project.exceptions.DataNotFoundException;
import com.github.ecommerce_project.models.Product;
import com.github.ecommerce_project.models.User;
import com.github.ecommerce_project.repositories.OrderRepository;
import com.github.ecommerce_project.repositories.ProductRepository;
import com.github.ecommerce_project.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("Throws error when user not found")
    void createOrder_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(100L)).thenReturn(Optional.empty());

        OrderRequestDto request = OrderRequestDto.builder()
                .address("123 Test Street")
                .orderItems(List.of(OrderItemRequestDto.builder()
                        .productId(1L)
                        .quantity(1)
                        .build()))
                .build();

        assertThrows(DataNotFoundException.class, () -> orderService.createOrder(100L, request));
    }

    @Test
    @DisplayName("Throws when a product in the order is not found")
    void createOrder_shouldThrow_whenProductInOrderNotFound() {
        User user = User.builder().id(1L).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        OrderRequestDto request = OrderRequestDto.builder()
                .address("123 Test Street")
                .orderItems(List.of(OrderItemRequestDto.builder()
                        .productId(99L)
                        .quantity(1)
                        .build()))
                .build();

        assertThrows(DataNotFoundException.class, () -> orderService.createOrder(1L, request));
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Throws when any product has insufficient stock")
    void createOrder_shouldThrow_whenAnyProductInsufficientStock() {
        User user = User.builder().id(1L).build();
        Product product = Product.builder()
                .id(1L)
                .stockQuantity(2)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        OrderRequestDto request = OrderRequestDto.builder()
                .address("123 Test Street")
                .orderItems(List.of(OrderItemRequestDto.builder()
                        .productId(1L)
                        .quantity(5)
                        .build()))
                .build();

        // validateStock method throws IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(1L, request));

        verify(orderRepository, never()).save(any());
        verify(productRepository, never()).save(any());

    }

}
