package com.example.ordermanagement.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "webhooks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Webhook extends BaseEntity {
    @Column(nullable = false)
    private String url;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String status;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "last_triggered_at")
    private Instant lastTriggeredAt;
} 