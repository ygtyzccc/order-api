package com.example.ordermanagement.domain.repository;

import com.example.ordermanagement.domain.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    Optional<Order> findByOrderNumber(UUID orderNumber);

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
} 