package com.example.order.mapper;

import com.example.order.domain.Order;
import com.example.order.domain.OrderItem;
import com.example.order.dto.OrderDto;
import com.example.order.dto.OrderItemDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderDto toDto(Order order);
    OrderItemDto toDto(OrderItem orderItem);
} 