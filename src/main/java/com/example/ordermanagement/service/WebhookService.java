package com.example.ordermanagement.service;

import com.example.ordermanagement.domain.model.Order;

public interface WebhookService {
    void notifyOrderCreated(Order order);

    void notifyOrderCancelled(Order order);
} 