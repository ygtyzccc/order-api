package com.example.ordermanagement.service.impl;

import com.example.ordermanagement.domain.model.Order;
import com.example.ordermanagement.domain.model.Webhook;
import com.example.ordermanagement.domain.repository.WebhookRepository;
import com.example.ordermanagement.service.WebhookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookServiceImpl implements WebhookService {
    private final WebhookRepository webhookRepository;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Async
    @Override
    public void notifyOrderCreated(Order order) {
        sendWebhookNotification("ORDER_CREATED", order);
    }

    @Async
    @Override
    public void notifyOrderCancelled(Order order) {
        sendWebhookNotification("ORDER_CANCELLED", order);
    }

    private void sendWebhookNotification(String eventType, Order order) {
        webhookRepository.findByEventType(eventType)
                .forEach(webhook -> {
                    try {
                        String payload = objectMapper.writeValueAsString(order);

                        webClientBuilder.build()
                                .post()
                                .uri(webhook.getUrl())
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(payload)
                                .retrieve()
                                .toBodilessEntity()
                                .subscribe(
                                        response -> {
                                            log.info("Webhook notification sent successfully: {}", webhook.getUrl());
                                            updateWebhookStatus(webhook, "SUCCESS");
                                        },
                                        error -> {
                                            log.error("Failed to send webhook notification: {}", error.getMessage());
                                            updateWebhookStatus(webhook, "FAILED");
                                        }
                                );
                    } catch (Exception e) {
                        log.error("Error processing webhook notification", e);
                        updateWebhookStatus(webhook, "FAILED");
                    }
                });
    }

    private void updateWebhookStatus(Webhook webhook, String status) {
        webhook.setStatus(status);
        webhook.setLastTriggeredAt(Instant.now());
        if ("FAILED".equals(status)) {
            webhook.setRetryCount(webhook.getRetryCount() + 1);
        }
        webhookRepository.save(webhook);
    }
} 