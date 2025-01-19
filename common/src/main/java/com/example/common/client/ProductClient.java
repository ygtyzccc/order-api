package com.example.common.client;

import com.example.common.dto.ProductDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ProductClient {
    private final WebClient.Builder webClientBuilder;
    
    @Value("${app.product-service.url}")
    private String productServiceUrl;

    public ProductDto getProduct(Long id) {
        return webClientBuilder.baseUrl(productServiceUrl)
                .build()
                .get()
                .uri("/api/v1/products/{id}", id)
                .retrieve()
                .onStatus(status -> status.value() == 404,
                        response -> Mono.error(new EntityNotFoundException("Product not found")))
                .bodyToMono(ProductDto.class)
                .block();
    }

    public ProductDto updateStock(Long id, Integer quantity) {
        return webClientBuilder.baseUrl(productServiceUrl)
                .build()
                .put()
                .uri("/api/v1/products/{id}/stock?quantity={quantity}", id, quantity)
                .retrieve()
                .onStatus(status -> status.value() == 404,
                        response -> Mono.error(new EntityNotFoundException("Product not found")))
                .bodyToMono(ProductDto.class)
                .block();
    }
} 