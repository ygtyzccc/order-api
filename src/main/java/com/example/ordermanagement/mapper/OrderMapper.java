package com.example.ordermanagement.mapper;

import com.example.ordermanagement.domain.model.Order;
import com.example.ordermanagement.dto.OrderDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface OrderMapper {

    @Mapping(target = "userId", source = "user.id")
    OrderDto toDto(Order order);

    @Named("toDtoWithoutItems")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "items", ignore = true)
    OrderDto toDtoWithoutItems(Order order);
} 