package com.github.ecommerce_project.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.ecommerce_project.models.Order;

import java.time.LocalDateTime;
import java.util.Optional;

import com.github.ecommerce_project.models.enums.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserId(Long userId, Pageable p);

    Page<Order> findByStatus(OrderStatus status, Pageable p);

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByOrderDateBetween(LocalDateTime start, LocalDateTime end, Pageable p);

}
