package com.example.order.client;

import com.example.common.dto.UserDto;
import com.example.order.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@RequiredArgsConstructor
public class UserWebClient {
    private final WebClient userServiceWebClient;

    public UserDto getUser(Long userId) {
        try {
            return userServiceWebClient.get()
                    .uri("/api/v1/users/{id}", userId)
                    .retrieve()
                    .bodyToMono(UserDto.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
    }
} 