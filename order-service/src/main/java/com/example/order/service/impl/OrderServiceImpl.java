package com.example.order.service.impl;

import com.example.order.domain.OrderStatus;
import com.example.order.dto.CreateOrderRequest;
import com.example.order.dto.OrderDto;
import com.example.order.dto.OrderSearchRequest;
import com.example.order.exception.ResourceNotFoundException;
import com.example.order.mapper.OrderMapper;
import com.example.order.repository.OrderRepository;
import com.example.order.service.OrderEventService;
import com.example.order.service.OrderSagaService;
import com.example.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderEventService orderEventService;
    private final OrderSagaService orderSagaService;

    @Override
    @Transactional
    public OrderDto createOrder(CreateOrderRequest request) {
        return orderSagaService.createOrder(request);
    }

    @Override
    @Transactional
    public void cancelOrder(UUID orderNumber) {
        var order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        
        orderEventService.publishOrderEvent(orderNumber, OrderStatus.CANCELLED);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrder(UUID orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .map(orderMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> searchOrders(OrderSearchRequest request) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(request.getUserId())
                .stream()
                .map(orderMapper::toDto)
                .toList();
    }
} 