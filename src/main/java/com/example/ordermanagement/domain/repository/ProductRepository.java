package com.example.ordermanagement.domain.repository;

import com.example.ordermanagement.domain.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
} 