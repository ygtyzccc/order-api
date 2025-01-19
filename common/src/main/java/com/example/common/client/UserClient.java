package com.example.common.client;

import com.example.common.dto.UserDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserClient {
    private final WebClient.Builder webClientBuilder;
    
    @Value("${app.user-service.url}")
    private String userServiceUrl;

    public UserDto getUser(Long id) {
        return webClientBuilder.baseUrl(userServiceUrl)
                .build()
                .get()
                .uri("/api/v1/users/{id}", id)
                .retrieve()
                .onStatus(status -> status.value() == 404,
                        response -> Mono.error(new EntityNotFoundException("User not found")))
                .bodyToMono(UserDto.class)
                .block();
    }
} 