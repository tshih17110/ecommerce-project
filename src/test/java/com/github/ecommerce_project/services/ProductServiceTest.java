package com.github.ecommerce_project.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.github.ecommerce_project.dtos.product.ProductRequestDto;
import com.github.ecommerce_project.dtos.product.ProductResponseDto;
import com.github.ecommerce_project.exceptions.DataNotFoundException;
import com.github.ecommerce_project.mapper.ProductMapper;
import com.github.ecommerce_project.models.Product;
import com.github.ecommerce_project.repositories.ProductRepository;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductRequestDto request;

    @BeforeEach
    void setUp() {

        product = Product.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("25.00"))
                .stockQuantity(10)
                .build();

        request = ProductRequestDto.builder()
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("25.00"))
                .stockQuantity(10)
                .build();
    }

    @Nested
    @DisplayName("createProduct")
    class CreateProduct {

        @Test
        @DisplayName("Throws when product name already exists")
        void createProduct_shouldThrow_whenProductNameAlreadyExists() {
            when(productRepository.existsByName("Test Product")).thenReturn(true);
            assertThrows(IllegalArgumentException.class, () -> productService.createProduct(request));
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Saves and returns DTO when name is unique")
        void createProduct_shouldReturnDto_whenProductNameIsUnique() {
            ProductResponseDto expectedDto = ProductResponseDto.builder()
                    .name("Test Product")
                    .description("Test Description")
                    .price(new BigDecimal("25.00"))
                    .inStock(true)
                    .build();

            when(productRepository.existsByName("Test Product")).thenReturn(false);
            when(productMapper.toProduct(request)).thenReturn(product);
            when(productRepository.save(product)).thenReturn(product);
            when(productMapper.toDto(product)).thenReturn(expectedDto);

            ProductResponseDto result = productService.createProduct(request);

            assertEquals("Test Product", result.getName());
            assertEquals(new BigDecimal("25.00"), result.getPrice());
            assertTrue(result.isInStock());
            verify(productRepository).save(product);

        }

    }

    @Nested
    @DisplayName("getProductDetails")
    class GetProductDetails {

        @Test
        @DisplayName("Returns DTO when product found")
        void getProductDetails_shouldReturnDto_whenProductFound() {

            ProductResponseDto expectedDto = ProductResponseDto.builder()
                    .name("Test Product")
                    .description("Test Description")
                    .price(new BigDecimal("25.00"))
                    .inStock(true)
                    .build();

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(productMapper.toDto(product)).thenReturn(expectedDto);

            ProductResponseDto result = productService.getProductDetails(1L);

            assertEquals("Test Product", result.getName());
            assertEquals(new BigDecimal("25.00"), result.getPrice());
            assertTrue(result.isInStock());
        }

        @Test
        @DisplayName("Throws when product ID not found")
        void getProductDetails_shouldThrow_whenProductNotFound() {
            when(productRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(DataNotFoundException.class, () -> productService.getProductDetails(1L));
        }

    }

    @Nested
    @DisplayName("getAllProducts")
    class GetAllProducts {

        @Test
        @DisplayName("Returns mapped page from repository")
        void getAllProducts_shouldReturnPage_whenCalled() {

            ProductResponseDto response = ProductResponseDto.builder()
                    .name("Test Product")
                    .description("Test Description")
                    .price(new BigDecimal("25.00"))
                    .inStock(true)
                    .build();

            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);

            when(productRepository.findAll(pageable)).thenReturn(productPage);
            when(productMapper.toDto(product)).thenReturn(response);

            Page<ProductResponseDto> result = productService.getAllProducts(pageable);

            assertEquals(1, result.getTotalElements());
            assertEquals(1, result.getContent().size());
            assertEquals("Test Product", result.getContent().get(0).getName());
            assertEquals(new BigDecimal("25.00"), result.getContent().get(0).getPrice());
            assertTrue(result.getContent().get(0).isInStock());
            verify(productRepository).findAll(pageable);

        }

    }

    @Nested
    @DisplayName("updateProduct")
    class UpdateProduct {

        @Test
        @DisplayName("Throws when product ID not found")
        void updateProduct_shouldThrow_whenProductIdNotFound() {

            when(productRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(DataNotFoundException.class, () -> productService.updateProduct(1L, request));
        }

        @Test
        @DisplayName("Updates existing product and saves")
        void updateProduct_shouldReturnDto_whenProductIsUpdated() {

            ProductRequestDto updateRequest = ProductRequestDto.builder()
                    .name("Updated Product")
                    .description("Updated Description")
                    .price(new BigDecimal("30.00"))
                    .stockQuantity(0)
                    .build();

            ProductResponseDto expectedDto = ProductResponseDto.builder()
                    .name("Updated Product")
                    .description("Updated Description")
                    .price(new BigDecimal("30.00"))
                    .inStock(false)
                    .build();

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(productRepository.save(product)).thenReturn(product);
            when(productMapper.toDto(product)).thenReturn(expectedDto);

            ProductResponseDto result = productService.updateProduct(1L, updateRequest);

            assertEquals("Updated Product", result.getName());
            assertEquals("Updated Description", result.getDescription());
            assertEquals(new BigDecimal("30.00"), result.getPrice());
            assertFalse(result.isInStock());
            verify(productMapper).updateEntityFromDto(updateRequest, product);
            verify(productRepository).save(product);
        }
    }

    @Nested
    @DisplayName("deleteProduct")
    class DeleteProduct {

        @Test
        @DisplayName("Throws when product ID not found")
        void deleteProduct_shouldThrow_whenProductIdNotFound() {
            when(productRepository.existsById(1L)).thenReturn(false);

            assertThrows(DataNotFoundException.class, () -> productService.deleteProduct(1L));
        }

        @Test
        @DisplayName("Should delete product when exists")
        void deleteProduct_shouldDeleteProduct_whenProductExists() {
            when(productRepository.existsById(1L)).thenReturn(true);

            productService.deleteProduct(1L);

            verify(productRepository).deleteById(1L);
        }
    }

}

// **`deleteProduct`**

// - Throws when product ID not found
// - Calls `deleteById` when product exists
