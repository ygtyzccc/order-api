package com.example.order.service.impl;

import com.example.common.client.ProductClient;
import com.example.common.dto.ProductDto;
import com.example.order.domain.OrderItem;
import com.example.order.dto.CreateOrderRequest;
import com.example.order.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {
    private final ProductClient productClient;

    @Override
    public List<OrderItem> createOrderItems(List<CreateOrderRequest.OrderItemRequest> itemRequests) {
        return itemRequests.stream()
                .map(this::createOrderItem)
                .toList();
    }

    @Override
    public BigDecimal calculateTotalAmount(List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private OrderItem createOrderItem(CreateOrderRequest.OrderItemRequest itemRequest) {
        ProductDto product = productClient.getProduct(itemRequest.getProductId());

        return OrderItem.builder()
                .productId(product.getId())
                .quantity(itemRequest.getQuantity())
                .unitPrice(product.getPrice())
                .totalPrice(calculateItemTotal(product.getPrice(), itemRequest.getQuantity()))
                .build();
    }

    private BigDecimal calculateItemTotal(BigDecimal price, Integer quantity) {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
} 