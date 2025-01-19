package com.example.order.service;

import com.example.order.dto.CreateOrderRequest;
import com.example.order.dto.OrderDto;
import com.example.order.dto.OrderSearchRequest;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderDto createOrder(CreateOrderRequest request);
    OrderDto getOrder(UUID orderNumber);
    void cancelOrder(UUID orderNumber);
    List<OrderDto> searchOrders(OrderSearchRequest request);
} 