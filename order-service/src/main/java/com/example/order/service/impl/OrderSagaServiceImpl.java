package com.example.order.service.impl;

import com.example.common.event.OrderSagaEvent;
import com.example.common.event.OrderSagaEvent.OrderSagaStatus;
import com.example.order.client.ProductWebClient;
import com.example.order.domain.Order;
import com.example.order.domain.OrderStatus;
import com.example.order.dto.CreateOrderRequest;
import com.example.order.dto.OrderDto;
import com.example.order.exception.InsufficientStockException;
import com.example.order.mapper.OrderMapper;
import com.example.order.repository.OrderRepository;
import com.example.order.service.OrderEventService;
import com.example.order.service.OrderItemService;
import com.example.order.service.OrderSagaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSagaServiceImpl implements OrderSagaService {
    private final ProductWebClient productWebClient;
    private final OrderItemService orderItemService;
    private final OrderEventService orderEventService;
    private final OrderMapper orderMapper;
    private final KafkaTemplate<String, OrderSagaEvent> kafkaTemplate;
    private final OrderRepository orderRepository;

    private static final String SAGA_TOPIC = "order-saga-events";

    @Override
    @Transactional
    public OrderDto createOrder(CreateOrderRequest request) {
        UUID orderNumber = UUID.randomUUID();
        var sagaEvent = initializeSagaEvent(orderNumber, request);

        try {
            // Step 1: Validate stock
            validateStockAvailability(request);

            // Step 2: Update stock and publish event
            updateStocks(request, sagaEvent);
            publishSagaEvent(sagaEvent);

            // Step 3: Create and save order
            var order = createOrderEntity(request, orderNumber);
            var savedOrder = orderRepository.save(order);
            var orderDto = orderMapper.toDto(savedOrder);

            // Step 4: Complete saga
            completeSaga(sagaEvent);

            return orderDto;
        } catch (Exception e) {
            // If anything fails, trigger compensation
            sagaEvent.setStatus(OrderSagaStatus.FAILED);
            publishSagaEvent(sagaEvent);
            throw e;
        }
    }



    @Override
    @Transactional
    public void handleStockUpdateCompensation(OrderSagaEvent event) {
        log.info("Compensating stock updates for order: {}", event.getOrderNumber());
        event.getItems().forEach(item -> {
            try {
                productWebClient.updateStock(item.getProductId(), item.getOriginalStock());
            } catch (Exception e) {
                log.error("Failed to compensate stock for product {}: {}", 
                    item.getProductId(), e.getMessage());
            }
        });
        event.setStatus(OrderSagaStatus.COMPENSATED);
        publishSagaEvent(event);
    }

    @Override
    @Transactional
    public void handleOrderCreationCompensation(OrderSagaEvent event) {
        // Implementation for order compensation if needed
        log.info("Compensating order creation for order: {}", event.getOrderNumber());
        event.setStatus(OrderSagaStatus.COMPENSATED);
        publishSagaEvent(event);
    }

    private OrderSagaEvent initializeSagaEvent(UUID orderNumber, CreateOrderRequest request) {
        return OrderSagaEvent.builder()
                .orderNumber(orderNumber)
                .eventType("CREATE_ORDER")
                .status(OrderSagaStatus.STARTED)
                .items(new ArrayList<>())
                .build();
    }

    private void validateStockAvailability(CreateOrderRequest request) {
        for (var item : request.getItems()) {
            var product = productWebClient.getProduct(item.getProductId());
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for product: " + product.getName()
                );
            }
        }
    }

    private void updateStocks(CreateOrderRequest request, OrderSagaEvent sagaEvent) {
        for (var item : request.getItems()) {
            var product = productWebClient.getProduct(item.getProductId());
            int newStockQuantity = product.getStockQuantity() - item.getQuantity();

            sagaEvent.getItems().add(OrderSagaEvent.OrderItemEvent.builder()
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .originalStock(product.getStockQuantity())
                    .build());

            productWebClient.updateStock(item.getProductId(), newStockQuantity);
        }
        sagaEvent.setStatus(OrderSagaStatus.STOCK_UPDATED);
    }

    private Order createOrderEntity(CreateOrderRequest request, UUID orderNumber) {
        var order = Order.builder()
                .orderNumber(orderNumber)
                .userId(request.getUserId())
                .status(OrderStatus.PENDING)
                .build();

        var orderItems = orderItemService.createOrderItems(request.getItems());
        orderItems.forEach(order::addItem);
        order.setTotalAmount(orderItemService.calculateTotalAmount(orderItems));

        return order;
    }

    private void completeSaga(OrderSagaEvent sagaEvent) {
        sagaEvent.setStatus(OrderSagaStatus.COMPLETED);
        publishSagaEvent(sagaEvent);
        orderEventService.publishOrderEvent(sagaEvent.getOrderNumber(), OrderStatus.PENDING);
    }

    private void publishSagaEvent(OrderSagaEvent event) {
        try {
            kafkaTemplate.send(SAGA_TOPIC, event.getOrderNumber().toString(), event);
        } catch (Exception e) {
            log.error("Failed to publish saga event: {}", e.getMessage());
            throw e;
        }
    }
} 