package com.github.ecommerce_project.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.github.ecommerce_project.dtos.cart.CartResponseDto;
import com.github.ecommerce_project.models.Cart;

@Mapper(componentModel = "spring", uses = { CartItemMapper.class })
public interface CartMapper {

    @Mapping(target = "totalCartPrice", ignore = true)
    @Mapping(target = "totalItemCount", ignore = true)
    CartResponseDto toDto(Cart cart);

}
