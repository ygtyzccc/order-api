package com.example.product.service;

import com.example.common.dto.ProductDto;
import com.example.product.dto.CreateProductRequest;

import java.util.List;

public interface ProductService {
    ProductDto createProduct(CreateProductRequest request);
    ProductDto getProduct(Long id);
    List<ProductDto> getAllProducts();
    ProductDto updateStock(Long id, Integer quantity);
    void deleteProduct(Long id);
} 