package com.example.order.client;

import com.example.common.dto.ProductDto;
import com.example.order.exception.ProductServiceException;
import com.example.order.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@RequiredArgsConstructor
public class ProductWebClient {
    private final WebClient productServiceWebClient;

    public ProductDto getProduct(Long productId) {
        try {
            return productServiceWebClient.get()
                    .uri("/api/v1/products/{id}", productId)
                    .retrieve()
                    .bodyToMono(ProductDto.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        } catch (Exception e) {
            throw new ProductServiceException("Failed to get product information: " + e.getMessage());
        }
    }

    public void updateStock(Long productId, Integer quantity) {
        try {
            productServiceWebClient.put()
                    .uri("/api/v1/products/{id}/stock?quantity={quantity}", 
                        productId, quantity)
                    .retrieve()
                    .bodyToMono(ProductDto.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        } catch (Exception e) {
            throw new ProductServiceException("Failed to update product stock: " + e.getMessage());
        }
    }
} 