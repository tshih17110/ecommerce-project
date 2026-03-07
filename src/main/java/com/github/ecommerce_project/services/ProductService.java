package com.github.ecommerce_project.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.ecommerce_project.dtos.product.ProductRequestDto;
import com.github.ecommerce_project.dtos.product.ProductResponseDto;
import com.github.ecommerce_project.exceptions.DataNotFoundException;
import com.github.ecommerce_project.mapper.ProductMapper;
import com.github.ecommerce_project.models.Product;
import com.github.ecommerce_project.repositories.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto newProductDto) {
        if (productRepository.existsByName(newProductDto.getName())) {
            throw new IllegalArgumentException("Product name already exists");
        }
        Product newProduct = productMapper.toProduct(newProductDto);
        Product savedProduct = productRepository.save(newProduct);
        return productMapper.toDto(savedProduct);
    }

    @Transactional(readOnly = true)
    public Product getProductDetails(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Product not found"));
    }

    @Transactional
    public ProductResponseDto updateProduct(Long id, ProductRequestDto updateDto) {

        // Product to be updated
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Product not found"));

        // Update existing product
        productMapper.updateEntityFromDto(updateDto, existingProduct);

        // Save and return updated product
        return productMapper.toDto(productRepository.save(existingProduct));
    }

}
