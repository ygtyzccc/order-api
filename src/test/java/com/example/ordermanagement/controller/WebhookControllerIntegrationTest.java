package com.example.ordermanagement.controller;

import com.example.ordermanagement.config.TestContainersConfig;
import com.example.ordermanagement.domain.model.Webhook;
import com.example.ordermanagement.dto.WebhookRegistrationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class WebhookControllerIntegrationTest extends TestContainersConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerWebhook_Success() throws Exception {
        var request = WebhookRegistrationRequest.builder()
                .url("http://example.com/webhook")
                .eventType("ORDER_CREATED")
                .build();

        mockMvc.perform(post("/api/v1/webhooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").value("http://example.com/webhook"))
                .andExpect(jsonPath("$.eventType").value("ORDER_CREATED"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.retryCount").value(0));
    }

    @Test
    void registerWebhook_InvalidUrl() throws Exception {
        var request = WebhookRegistrationRequest.builder()
                .url("invalid-url")
                .eventType("ORDER_CREATED")
                .build();

        mockMvc.perform(post("/api/v1/webhooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerWebhook_InvalidEventType() throws Exception {
        var request = WebhookRegistrationRequest.builder()
                .url("http://example.com/webhook")
                .eventType("INVALID_EVENT")
                .build();

        mockMvc.perform(post("/api/v1/webhooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteWebhook_Success() throws Exception {
        // First create a webhook
        var request = WebhookRegistrationRequest.builder()
                .url("http://example.com/webhook")
                .eventType("ORDER_CREATED")
                .build();

        var result = mockMvc.perform(post("/api/v1/webhooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        var webhook = objectMapper.readValue(result.getResponse().getContentAsString(), Webhook.class);

        // Then delete it
        mockMvc.perform(delete("/api/v1/webhooks/{id}", webhook.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteWebhook_NotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/webhooks/{id}", 999L))
                .andExpect(status().isNotFound());
    }
} 