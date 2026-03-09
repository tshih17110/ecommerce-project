package com.github.ecommerce_project.dtos.auth;

import com.github.ecommerce_project.dtos.user.UserResponseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDto {
    private String token;
    private UserResponseDto user;
}
