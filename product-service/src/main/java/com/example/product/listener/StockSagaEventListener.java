package com.example.product.listener;

import com.example.common.event.OrderSagaEvent;
import com.example.product.service.ProductSagaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockSagaEventListener {
    private final ProductSagaService productSagaService;

    @KafkaListener(topics = "order-saga-events", groupId = "product-service")
    public void handleOrderSagaEvent(OrderSagaEvent event) {
        log.info("Received order saga event: {}", event);
        
        if (event.getStatus() == OrderSagaEvent.OrderSagaStatus.FAILED) {
            productSagaService.compensateStockUpdate(event);
        }
    }
} 