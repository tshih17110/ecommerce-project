package com.github.ecommerce_project.dtos.orderItem;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemRequestDto {

    @NotNull(message = "Product ID required")
    private Long productId;

    @NotNull
    @Min(value = 1)
    private Integer quantity;

}
