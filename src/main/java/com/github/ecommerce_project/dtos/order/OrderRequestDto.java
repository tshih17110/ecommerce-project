package com.github.ecommerce_project.dtos.order;

import java.util.List;

import com.github.ecommerce_project.dtos.orderItem.OrderItemRequestDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequestDto {

    @NotBlank
    private String address;

    @NotEmpty
    private List<@Valid OrderItemRequestDto> orderItems;

}
