package com.github.ecommerce_project.dtos.orderItem;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemResponseDto {

    private Long id;

    private String productName;

    private Integer quantity;

    private BigDecimal priceAtPurchase;

    private BigDecimal subtotal;

}
