package com.example.order.listener;

import com.example.common.event.OrderSagaEvent;
import com.example.order.service.OrderSagaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSagaEventListener {
    private final OrderSagaService orderSagaService;

    @KafkaListener(topics = "order-saga-events", groupId = "order-service")
    public void handleSagaEvent(OrderSagaEvent event) {
        log.info("Received saga event: {}", event);
        
        if (event.getStatus() == OrderSagaEvent.OrderSagaStatus.FAILED) {
            switch (event.getEventType()) {
                case "CREATE_ORDER" -> orderSagaService.handleStockUpdateCompensation(event);
                // Add more cases for other saga types
                default -> log.warn("Unknown event type: {}", event.getEventType());
            }
        }
    }
} 