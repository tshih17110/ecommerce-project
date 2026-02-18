package com.github.ecommerce_project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.ecommerce_project.models.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

}
