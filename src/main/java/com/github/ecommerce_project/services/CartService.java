package com.github.ecommerce_project.services;

import org.springframework.stereotype.Service;

import com.github.ecommerce_project.repositories.CartItemRepository;
import com.github.ecommerce_project.repositories.CartRepository;

@Service
public class CartService {

    private CartRepository cartRepository;
    private CartItemRepository cartItemRepository;

}
