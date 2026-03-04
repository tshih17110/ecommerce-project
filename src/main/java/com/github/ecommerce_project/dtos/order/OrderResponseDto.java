package com.github.ecommerce_project.dtos.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.github.ecommerce_project.dtos.orderItem.OrderItemResponseDto;
import com.github.ecommerce_project.models.enums.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDto {

    private Long orderId;

    private OrderStatus status;

    private BigDecimal totalAmount;

    private LocalDateTime orderDate;

    private List<OrderItemResponseDto> orderItems;
}
