package com.github.ecommerce_project.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.github.ecommerce_project.dtos.orderItem.OrderItemResponseDto;
import com.github.ecommerce_project.models.OrderItem;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(source = "product.name", target = "productName")
    @Mapping(target = "subtotal", expression = "java(orderItem.getPriceAtPurchase().multiply(java.math.BigDecimal.valueOf(orderItem.getQuantity())))")
    OrderItemResponseDto toDto(OrderItem orderItem);

}
