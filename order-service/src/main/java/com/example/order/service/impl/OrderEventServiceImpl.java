package com.example.order.service.impl;

import com.example.order.domain.OrderStatus;
import com.example.order.event.OrderEvent;
import com.example.order.service.OrderEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderEventServiceImpl implements OrderEventService {
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Override
    public void publishOrderEvent(UUID orderNumber, OrderStatus status) {
        kafkaTemplate.send("orders", orderNumber.toString(), 
                new OrderEvent(orderNumber, status));
    }
} 