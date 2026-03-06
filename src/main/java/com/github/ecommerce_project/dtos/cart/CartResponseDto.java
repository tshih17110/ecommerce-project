package com.github.ecommerce_project.dtos.cart;

import java.math.BigDecimal;
import java.util.List;

import com.github.ecommerce_project.dtos.cartItem.CartItemResponseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartResponseDto {

    private Long id;

    private List<CartItemResponseDto> items;

    private BigDecimal totalCartPrice;

    private Integer totalItemCount;

}
