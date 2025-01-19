package com.example.product.service.impl;

import com.example.common.event.OrderSagaEvent;
import com.example.product.event.StockSagaEvent;
import com.example.product.repository.ProductRepository;
import com.example.product.service.ProductSagaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSagaServiceImpl implements ProductSagaService {
    private final ProductRepository productRepository;
    private final KafkaTemplate<String, StockSagaEvent> kafkaTemplate;
    
    private static final String STOCK_SAGA_TOPIC = "stock-saga-events";

    @Override
    @Transactional
    public void compensateStockUpdate(OrderSagaEvent event) {
        event.getItems().forEach(item -> {
            try {
                var product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
                
                // Restore original stock
                product.setStockQuantity(item.getOriginalStock());
                productRepository.save(product);
                
                // Publish compensation event
                publishStockCompensationEvent(event.getOrderNumber(), item);
                
                log.info("Successfully compensated stock for product: {}", item.getProductId());
            } catch (Exception e) {
                log.error("Failed to compensate stock for product {}: {}", 
                    item.getProductId(), e.getMessage());
                throw e;
            }
        });
    }

    private void publishStockCompensationEvent(UUID orderNumber, OrderSagaEvent.OrderItemEvent item) {
        var stockEvent = StockSagaEvent.builder()
            .orderNumber(orderNumber)
            .eventType("STOCK_COMPENSATION")
            .status(StockSagaEvent.StockSagaStatus.COMPENSATED)
            .productId(item.getProductId())
            .quantity(item.getQuantity())
            .originalStock(item.getOriginalStock())
            .build();

        kafkaTemplate.send(STOCK_SAGA_TOPIC, orderNumber.toString(), stockEvent);
    }
} 