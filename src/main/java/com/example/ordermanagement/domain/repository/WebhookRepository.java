package com.example.ordermanagement.domain.repository;

import com.example.ordermanagement.domain.model.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WebhookRepository extends JpaRepository<Webhook, Long> {
    List<Webhook> findByEventType(String eventType);
} 