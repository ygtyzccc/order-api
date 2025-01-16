package com.example.ordermanagement.controller;

import com.example.ordermanagement.domain.model.Webhook;
import com.example.ordermanagement.domain.repository.WebhookRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Webhook configuration endpoints")
public class WebhookController {
    private final WebhookRepository webhookRepository;

    @PostMapping
    @Operation(summary = "Register a new webhook")
    public ResponseEntity<Webhook> registerWebhook(@Valid @RequestBody Webhook webhook) {
        webhook.setRetryCount(0);
        webhook.setStatus("ACTIVE");
        return new ResponseEntity<>(webhookRepository.save(webhook), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a webhook")
    public ResponseEntity<Void> deleteWebhook(@PathVariable Long id) {
        webhookRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
} 