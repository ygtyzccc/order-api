package com.example.order.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSagaEvent {
    private UUID orderNumber;
    private String eventType;
    private OrderSagaStatus status;
    private List<OrderItemEvent> items;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemEvent {
        private Long productId;
        private Integer quantity;
        private Integer originalStock;
    }
    
    public enum OrderSagaStatus {
        STARTED,
        STOCK_UPDATED,
        COMPLETED,
        FAILED,
        COMPENSATED
    }
} 