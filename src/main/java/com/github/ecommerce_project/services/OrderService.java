package com.github.ecommerce_project.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.ecommerce_project.repositories.OrderRepository;

@Service
public class OrderService {

    private OrderRepository orderRepository;

}
