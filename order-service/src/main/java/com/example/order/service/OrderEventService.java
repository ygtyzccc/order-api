package com.example.order.service;

import com.example.order.domain.OrderStatus;

import java.util.UUID;

public interface OrderEventService {
    void publishOrderEvent(UUID orderNumber, OrderStatus status);
} 