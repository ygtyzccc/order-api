package com.example.ordermanagement.service;

import com.example.ordermanagement.dto.CreateOrderRequest;
import com.example.ordermanagement.dto.OrderDto;
import com.example.ordermanagement.dto.OrderSearchRequest;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderDto createOrder(CreateOrderRequest request);

    OrderDto getOrder(UUID orderNumber);

    void cancelOrder(UUID orderNumber);

    List<OrderDto> searchOrders(OrderSearchRequest request);
} 