package com.example.ordermanagement.service;

import com.example.ordermanagement.dto.CreateProductRequest;
import com.example.ordermanagement.dto.ProductDto;

import java.util.List;

public interface ProductService {
    ProductDto createProduct(CreateProductRequest request);

    ProductDto getProduct(Long id);

    List<ProductDto> getAllProducts();

    ProductDto updateStock(Long id, Integer quantity);

    void deleteProduct(Long id);
} 