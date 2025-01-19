package com.example.product.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockSagaEvent {
    private UUID orderNumber;
    private String eventType;
    private StockSagaStatus status;
    private Long productId;
    private Integer quantity;
    private Integer originalStock;
    
    public enum StockSagaStatus {
        STARTED,
        COMPLETED,
        FAILED,
        COMPENSATED
    }
} 