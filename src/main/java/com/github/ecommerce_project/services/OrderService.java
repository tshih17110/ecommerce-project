package com.github.ecommerce_project.services;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.ecommerce_project.dtos.order.OrderRequestDto;
import com.github.ecommerce_project.dtos.order.OrderResponseDto;
import com.github.ecommerce_project.dtos.orderItem.OrderItemRequestDto;
import com.github.ecommerce_project.exceptions.DataNotFoundException;
import com.github.ecommerce_project.mapper.OrderMapper;
import com.github.ecommerce_project.models.Order;
import com.github.ecommerce_project.models.OrderItem;
import com.github.ecommerce_project.models.Product;
import com.github.ecommerce_project.models.User;
import com.github.ecommerce_project.models.enums.OrderStatus;
import com.github.ecommerce_project.repositories.OrderRepository;
import com.github.ecommerce_project.repositories.ProductRepository;
import com.github.ecommerce_project.repositories.UserRepository;
import com.github.ecommerce_project.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponseDto createOrder(Long userId, OrderRequestDto newOrderDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found."));

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setAddress(newOrderDto.getAddress());
        order.setOrderNumber(generateOrderNumber());

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequestDto itemDto : newOrderDto.getOrderItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new DataNotFoundException("Product not found."));

            OrderItem item = OrderItem.builder()
                    .product(product)
                    .quantity(itemDto.getQuantity())
                    .priceAtPurchase(product.getPrice())
                    .order(order)
                    .build();

            order.addOrderItem(item);
            total = total.add(product.getPrice().multiply(new BigDecimal(itemDto.getQuantity())));

        }
        order.setTotalAmount(total);

        Order savedOrder = orderRepository.save(order);
        return orderMapper.toDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Order " + orderId + "not found."));

        Long authenticatedUserId = SecurityUtils.getAuthenticatedUserId();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !order.getUser().getId().equals(authenticatedUserId)) {
            throw new AccessDeniedException("You do not have permission to view this order.");
        }
        return orderMapper.toDto(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getUserOrders(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable)
                .map(orderMapper::toDto);
    }

    @Transactional
    public OrderResponseDto updateStatus(Long id, OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Order not found."));
        order.setStatus(newStatus);
        return orderMapper.toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderResponseDto cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Order " + orderId + " not found."));

        Long authenticatedUserId = SecurityUtils.getAuthenticatedUserId();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !order.getUser().getId().equals(authenticatedUserId)) {
            throw new AccessDeniedException("You do not have permission to cancel this order.");
        }

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel an order that has already been shipped or delivered.");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order is already cancelled.");
        }

        // Restore inventory
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            int restoredStock = product.getStockQuantity() + item.getQuantity();
            product.setStockQuantity(restoredStock);
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);

        Order savedOrder = orderRepository.save(order);
        return orderMapper.toDto(savedOrder);
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

}
