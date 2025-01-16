package com.example.ordermanagement.service;

import com.example.ordermanagement.domain.model.Order;
import com.example.ordermanagement.domain.model.Webhook;
import com.example.ordermanagement.domain.repository.WebhookRepository;
import com.example.ordermanagement.service.impl.WebhookServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

    @Mock
    private WebhookRepository webhookRepository;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private WebhookServiceImpl webhookService;

    private Webhook webhook;
    private Order order;

    @BeforeEach
    void setUp() {
        webhook = new Webhook();
        webhook.setUrl("http://example.com/webhook");
        webhook.setEventType("ORDER_CREATED");
        webhook.setStatus("ACTIVE");
        webhook.setRetryCount(0);

        order = new Order();

        // Setup WebClient mock chain
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void notifyOrderCreated_Success() throws Exception {
        // Arrange
        when(webhookRepository.findByEventType("ORDER_CREATED")).thenReturn(List.of(webhook));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(responseSpec.toBodilessEntity()).thenReturn(Mono.empty());

        // Act
        webhookService.notifyOrderCreated(order);

        // Assert
        verify(webhookRepository, timeout(1000)).save(argThat(w ->
                "SUCCESS".equals(w.getStatus()) && w.getRetryCount() == 0
        ));
    }

    @Test
    void notifyOrderCreated_Failed() throws Exception {
        // Arrange
        when(webhookRepository.findByEventType("ORDER_CREATED")).thenReturn(List.of(webhook));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(responseSpec.toBodilessEntity()).thenReturn(Mono.error(new RuntimeException("Failed")));

        // Act
        webhookService.notifyOrderCreated(order);

        // Assert
        verify(webhookRepository, timeout(1000)).save(argThat(w ->
                "FAILED".equals(w.getStatus()) && w.getRetryCount() == 1
        ));
    }

    @Test
    void notifyOrderCancelled_Success() throws Exception {
        // Arrange
        when(webhookRepository.findByEventType("ORDER_CANCELLED")).thenReturn(List.of(webhook));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(responseSpec.toBodilessEntity()).thenReturn(Mono.empty());

        // Act
        webhookService.notifyOrderCancelled(order);

        // Assert
        verify(webhookRepository, timeout(1000)).save(argThat(w ->
                "SUCCESS".equals(w.getStatus()) && w.getRetryCount() == 0
        ));
    }

    @Test
    void notifyOrderCreated_SerializationError() throws Exception {
        // Arrange
        when(webhookRepository.findByEventType("ORDER_CREATED")).thenReturn(List.of(webhook));
        when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("Serialization failed"));

        // Act
        webhookService.notifyOrderCreated(order);

        // Assert
        verify(webhookRepository, timeout(1000)).save(argThat(w ->
                "FAILED".equals(w.getStatus()) && w.getRetryCount() == 1
        ));
    }
} 