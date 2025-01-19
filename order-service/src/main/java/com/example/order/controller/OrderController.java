package com.example.order.controller;

import com.example.order.dto.CreateOrderRequest;
import com.example.order.dto.OrderDto;
import com.example.order.dto.OrderSearchRequest;
import com.example.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create a new order")
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return new ResponseEntity<>(orderService.createOrder(request), HttpStatus.CREATED);
    }

    @GetMapping("/{orderNumber}")
    @Operation(summary = "Get order by order number")
    public ResponseEntity<OrderDto> getOrder(@PathVariable UUID orderNumber) {
        return ResponseEntity.ok(orderService.getOrder(orderNumber));
    }

    @PostMapping("/{orderNumber}/cancel")
    @Operation(summary = "Cancel an order")
    public ResponseEntity<Void> cancelOrder(@PathVariable UUID orderNumber) {
        orderService.cancelOrder(orderNumber);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/search")
    @Operation(summary = "Search orders")
    public ResponseEntity<List<OrderDto>> searchOrders(@RequestBody OrderSearchRequest request) {
        return ResponseEntity.ok(orderService.searchOrders(request));
    }
} 