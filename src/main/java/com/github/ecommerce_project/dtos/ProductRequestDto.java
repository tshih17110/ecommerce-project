package com.github.ecommerce_project.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequestDto {

    @NotEmpty(message = "Invalid product name")
    @NotNull
    private String name;

    @NotEmpty(message = "Invalid product description")
    @NotNull
    @Size(max = 1000)
    private String description;

    @NotNull
    @DecimalMin(value = "0.0", message = "Invalid product price")
    private BigDecimal price;

    @NotNull(message = "Stock quantity required")
    @Min(value = 0, message = "Invalid stock quantity")
    private Integer stockQuantity;

}
