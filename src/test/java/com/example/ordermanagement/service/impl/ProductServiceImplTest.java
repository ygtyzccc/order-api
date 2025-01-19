package com.example.ordermanagement.service.impl;

import com.example.ordermanagement.domain.model.Product;
import com.example.ordermanagement.domain.repository.ProductRepository;
import com.example.ordermanagement.dto.CreateProductRequest;
import com.example.ordermanagement.dto.ProductDto;
import com.example.ordermanagement.exception.ResourceNotFoundException;
import com.example.ordermanagement.mapper.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;
    private ProductDto testProductDto;
    private CreateProductRequest createProductRequest;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setPrice(BigDecimal.valueOf(99.99));
        testProduct.setStockQuantity(10);
        testProduct.setId(1L); // Set the ID since we're testing with it
        // ID will be set by JPA in real scenarios

        testProductDto = ProductDto.builder()
                .id(1L)
                .name("Test Product")
                .price(BigDecimal.valueOf(99.99))
                .stockQuantity(10)
                .build();

        createProductRequest = new CreateProductRequest();
        createProductRequest.setName("Test Product");
        createProductRequest.setPrice(BigDecimal.valueOf(99.99));
        createProductRequest.setStockQuantity(10);
    }

    @Test
    void createProduct_Success() {
        when(productMapper.toEntity(any(CreateProductRequest.class))).thenReturn(testProduct);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(productMapper.toDto(any(Product.class))).thenReturn(testProductDto);

        var result = productService.createProduct(createProductRequest);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(testProduct.getName());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void getProduct_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productMapper.toDto(testProduct)).thenReturn(testProductDto);

        var result = productService.getProduct(1L);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(testProduct.getName());
        assertThat(result.getPrice()).isEqualTo(testProduct.getPrice());
        assertThat(result.getStockQuantity()).isEqualTo(testProduct.getStockQuantity());
    }

    @Test
    void getProduct_NotFound_ThrowsException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProduct(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void getAllProducts_Success() {
        var product2 = new Product();
        product2.setName("Test Product 2");
        product2.setPrice(BigDecimal.valueOf(199.99));
        product2.setStockQuantity(20);

        var productDto2 = ProductDto.builder()
                .id(2L)
                .name("Test Product 2")
                .price(BigDecimal.valueOf(199.99))
                .stockQuantity(20)
                .build();

        when(productRepository.findAll()).thenReturn(Arrays.asList(testProduct, product2));
        when(productMapper.toDto(testProduct)).thenReturn(testProductDto);
        when(productMapper.toDto(product2)).thenReturn(productDto2);

        var result = productService.getAllProducts();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo(testProduct.getName());
        assertThat(result.get(1).getName()).isEqualTo(product2.getName());
    }

    @Test
    void updateStock_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        
        // Create a product with updated stock
        var updatedProduct = new Product();
        updatedProduct.setName("Test Product");
        updatedProduct.setPrice(BigDecimal.valueOf(99.99));
        updatedProduct.setStockQuantity(15);
        
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);
        
        // Create DTO with updated stock
        var updatedProductDto = ProductDto.builder()
                .id(1L)
                .name("Test Product")
                .price(BigDecimal.valueOf(99.99))
                .stockQuantity(15)
                .build();
                
        when(productMapper.toDto(any(Product.class))).thenReturn(updatedProductDto);

        var result = productService.updateStock(1L, 15);

        assertThat(result).isNotNull();
        assertThat(result.getStockQuantity()).isEqualTo(15);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void deleteProduct_Success() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.deleteProduct(1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    void deleteProduct_NotFound_ThrowsException() {
        when(productRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> productService.deleteProduct(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
    }
} 