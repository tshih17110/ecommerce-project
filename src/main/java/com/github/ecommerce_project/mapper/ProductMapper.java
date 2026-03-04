package com.github.ecommerce_project.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.github.ecommerce_project.dtos.product.ProductRequestDto;
import com.github.ecommerce_project.dtos.product.ProductResponseDto;
import com.github.ecommerce_project.models.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "inStock", expression = "java(product.getStockQuantity() > 0)")
    ProductResponseDto toDto(Product product);

    @Mapping(target = "id", ignore = true)
    Product toProduct(ProductRequestDto dto);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(ProductRequestDto updateDto, @MappingTarget Product product);

}
