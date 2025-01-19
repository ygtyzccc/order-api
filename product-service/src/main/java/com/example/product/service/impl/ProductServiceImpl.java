package com.example.product.service.impl;

import com.example.common.dto.ProductDto;
import com.example.common.exception.ResourceNotFoundException;
import com.example.product.dto.CreateProductRequest;
import com.example.product.mapper.ProductMapper;
import com.example.product.repository.ProductRepository;
import com.example.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductDto createProduct(CreateProductRequest request) {
        var product = productMapper.toEntity(request);
        return productMapper.toDto(productRepository.save(product));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getProduct(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductDto updateStock(Long id, Integer quantity) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        product.setStockQuantity(quantity);
        return productMapper.toDto(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found");
        }
        productRepository.deleteById(id);
    }
} 