package com.github.ecommerce_project.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.github.ecommerce_project.dtos.order.OrderResponseDto;
import com.github.ecommerce_project.models.Order;

@Mapper(componentModel = "spring", uses = { OrderItemMapper.class })
public interface OrderMapper {

    @Mapping(source = "id", target = "orderId")
    OrderResponseDto toDto(Order order);
}
