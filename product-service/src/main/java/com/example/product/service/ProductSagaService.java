package com.example.product.service;

import com.example.common.event.OrderSagaEvent;

public interface ProductSagaService {
    void compensateStockUpdate(OrderSagaEvent event);
} 