package com.example.order.event;

import com.example.order.domain.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    private UUID orderNumber;
    private OrderStatus status;
} 