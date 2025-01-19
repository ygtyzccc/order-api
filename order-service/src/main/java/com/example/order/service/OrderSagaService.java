package com.example.order.service;

import com.example.common.event.OrderSagaEvent;
import com.example.order.dto.CreateOrderRequest;
import com.example.order.dto.OrderDto;

public interface OrderSagaService {
    OrderDto createOrder(CreateOrderRequest request);
    void handleStockUpdateCompensation(OrderSagaEvent event);
    void handleOrderCreationCompensation(OrderSagaEvent event);
} 