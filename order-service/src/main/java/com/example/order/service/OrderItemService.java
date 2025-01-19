package com.example.order.service;

import com.example.order.domain.OrderItem;
import com.example.order.dto.CreateOrderRequest;

import java.math.BigDecimal;
import java.util.List;

public interface OrderItemService {
    List<OrderItem> createOrderItems(List<CreateOrderRequest.OrderItemRequest> itemRequests);
    BigDecimal calculateTotalAmount(List<OrderItem> items);
} 