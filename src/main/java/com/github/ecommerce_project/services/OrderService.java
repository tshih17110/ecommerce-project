package com.github.ecommerce_project.services;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import com.github.ecommerce_project.models.enums.OrderStatus;
import com.github.ecommerce_project.repositories.OrderRepository;
import com.github.ecommerce_project.repositories.ProductRepository;

@Service
public class OrderService {

    private OrderRepository orderRepository;
    private ProductRepository productRepository;
    private OrderMapper orderMapper;

    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto newOrderDto) {

        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);
        order.setAddress(newOrderDto.getAddress());

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequestDto itemDto : newOrderDto.getOrderItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new DataNotFoundException("Product not found"));

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
    public OrderResponseDto getOrderById(Long id) {
        return orderRepository.findById(id)
                .map(orderMapper::toDto)
                .orElseThrow(() -> new DataNotFoundException("Order " + id + " not found."));
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

}
