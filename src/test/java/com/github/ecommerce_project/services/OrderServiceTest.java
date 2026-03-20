package com.github.ecommerce_project.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.ecommerce_project.dtos.order.OrderRequestDto;
import com.github.ecommerce_project.dtos.order.OrderResponseDto;
import com.github.ecommerce_project.dtos.orderItem.OrderItemRequestDto;
import com.github.ecommerce_project.exceptions.DataNotFoundException;
import com.github.ecommerce_project.mapper.OrderMapper;
import com.github.ecommerce_project.models.Order;
import com.github.ecommerce_project.models.Product;
import com.github.ecommerce_project.models.User;
import com.github.ecommerce_project.models.enums.OrderStatus;
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

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Product product;
    private OrderRequestDto request;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).build();

        product = Product.builder()
                .id(1L)
                .stockQuantity(10)
                .price(new BigDecimal("25.00"))
                .build();

        request = OrderRequestDto.builder()
                .address("123 Test Street")
                .orderItems(List.of(
                        OrderItemRequestDto.builder()
                                .productId(1L)
                                .quantity(2)
                                .build()))
                .build();
    }

    @Test
    @DisplayName("Throws error when user not found")
    void createOrder_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(DataNotFoundException.class, () -> orderService.createOrder(1L, request));
    }

    @Test
    @DisplayName("Throws when a product in the order is not found")
    void createOrder_shouldThrow_whenProductInOrderNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        OrderRequestDto testRequest = OrderRequestDto.builder()
                .address("123 Test Street")
                .orderItems(List.of(OrderItemRequestDto.builder()
                        .productId(1L)
                        .quantity(15)
                        .build()))
                .build();

        assertThrows(DataNotFoundException.class, () -> orderService.createOrder(1L, testRequest));
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Throws when any product has insufficient stock")
    void createOrder_shouldThrow_whenAnyProductInsufficientStock() {

        Product insufficientProduct = Product.builder()
                .id(1L)
                .stockQuantity(1)
                .price(new BigDecimal("25.00"))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(insufficientProduct));

        // validateStock method throws IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(1L, request));

        verify(orderRepository, never()).save(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deducts stock for each item correctly")
    void createOrder_shouldDeductStock_whenOrderIsPlaced() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        OrderRequestDto request = OrderRequestDto.builder()
                .address("123 Test Street")
                .orderItems(List.of(OrderItemRequestDto.builder()
                        .productId(1L)
                        .quantity(2)
                        .build()))
                .build();

        orderService.createOrder(1L, request);
        assertEquals(8, product.getStockQuantity());

        // Verifies change was persisted
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("Calculates total amount across multiple items correctly")
    void createorder_shouldCalculateTotalAmount_whenOrderIsPlaced() {
        Product product2 = Product.builder()
                .id(2L)
                .stockQuantity(3)
                .price(new BigDecimal("10.00"))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product2));

        OrderRequestDto request = OrderRequestDto.builder()
                .address("123 Test Street")
                .orderItems(List.of(
                        OrderItemRequestDto.builder()
                                .productId(1L)
                                .quantity(2)
                                .build(),
                        OrderItemRequestDto.builder()
                                .productId(2L)
                                .quantity(2)
                                .build()))
                .build();

        orderService.createOrder(1L, request);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertEquals(new BigDecimal("70.00"), captor.getValue().getTotalAmount());
    }

    @Test
    @DisplayName("Sets order status to PENDING on creation")
    void createOrder_shouldSetStatusToPending_whenOrderIsPlaced() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        orderService.createOrder(1L, request);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertEquals(OrderStatus.PENDING, captor.getValue().getStatus());
    }

    @Test
    @DisplayName("Saves order and returns mapped response DTO")
    void createOrder_shouldSaveOrderAndReturnMappedDTo_whenOrderIsPlaced() {
        OrderResponseDto expectedDto = OrderResponseDto.builder()
                .orderId(1L)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("50.00"))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(orderMapper.toDto(any(Order.class))).thenReturn(expectedDto);

        OrderResponseDto result = orderService.createOrder(1L, request);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());

        verify(orderMapper).toDto(any(Order.class));
        assertEquals(expectedDto.getOrderId(), result.getOrderId());
        assertEquals(expectedDto.getStatus(), result.getStatus());
        assertEquals(expectedDto.getTotalAmount(), result.getTotalAmount());
    }

}
