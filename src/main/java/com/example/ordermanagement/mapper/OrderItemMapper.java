package com.example.ordermanagement.mapper;

import com.example.ordermanagement.domain.model.OrderItem;
import com.example.ordermanagement.dto.OrderItemDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "productId", source = "product.id")
    OrderItemDto toDto(OrderItem orderItem);
} 