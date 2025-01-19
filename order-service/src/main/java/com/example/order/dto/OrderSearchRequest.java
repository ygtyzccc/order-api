package com.example.order.dto;

import com.example.order.domain.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSearchRequest {
    private Long userId;
    private OrderStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
} 