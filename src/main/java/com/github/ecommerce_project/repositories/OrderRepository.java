package com.github.ecommerce_project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.ecommerce_project.models.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

}
