package com.example.ordermanagement.service.impl;

import com.example.ordermanagement.domain.model.*;
import com.example.ordermanagement.domain.repository.OrderRepository;
import com.example.ordermanagement.domain.repository.ProductRepository;
import com.example.ordermanagement.domain.repository.UserRepository;
import com.example.ordermanagement.dto.CreateOrderRequest;
import com.example.ordermanagement.dto.OrderSearchRequest;
import com.example.ordermanagement.exception.InsufficientStockException;
import com.example.ordermanagement.exception.ResourceNotFoundException;
import com.example.ordermanagement.mapper.OrderItemMapper;
import com.example.ordermanagement.mapper.OrderMapper;
import com.example.ordermanagement.service.WebhookService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private WebhookService webhookService;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderItemMapper orderItemMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User testUser;
    private Product testProduct;
    private Order testOrder;
    private CreateOrderRequest createOrderRequest;
    private UUID orderNumber;

    @BeforeEach
    void setUp() {
        orderNumber = UUID.randomUUID();
        
        testUser = new User();
        testUser.setUsername("testuser");
        // ID will be set by JPA

        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setPrice(BigDecimal.valueOf(99.99));
        testProduct.setStockQuantity(10);
        // ID will be set by JPA

        testOrder = new Order();
        testOrder.setOrderNumber(orderNumber);
        testOrder.setUser(testUser);
        testOrder.setStatus(OrderStatus.PENDING);
        // ID will be set by JPA

        createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setUserId(1L);
        var orderItemRequest = new CreateOrderRequest.OrderItemRequest();
        orderItemRequest.setProductId(1L);
        orderItemRequest.setQuantity(2);
        createOrderRequest.setItems(Collections.singletonList(orderItemRequest));
    }

    @Test
    void createOrder_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        orderService.createOrder(createOrderRequest);

        verify(orderRepository).save(any(Order.class));
        verify(webhookService).notifyOrderCreated(any(Order.class));
    }

    @Test
    void createOrder_UserNotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(createOrderRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void createOrder_InsufficientStock_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        var productWithLowStock = new Product();
        productWithLowStock.setStockQuantity(1);
        // Other product properties...
        
        when(productRepository.findById(1L)).thenReturn(Optional.of(productWithLowStock));

        assertThatThrownBy(() -> orderService.createOrder(createOrderRequest))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void cancelOrder_Success() {
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.of(testOrder));

        orderService.cancelOrder(orderNumber);

        verify(orderRepository).save(any(Order.class));
        verify(webhookService).notifyOrderCancelled(any(Order.class));
        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void cancelOrder_OrderNotFound_ThrowsException() {
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.cancelOrder(orderNumber))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    void searchOrders_Success() {
        var searchRequest = new OrderSearchRequest();
        searchRequest.setUserId(1L);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(orderRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(testOrder)));

        var result = orderService.searchOrders(searchRequest);

        assertThat(result).isNotEmpty();
        verify(orderRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void searchOrders_UserNotFound_ThrowsException() {
        var searchRequest = new OrderSearchRequest();
        searchRequest.setUserId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.searchOrders(searchRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }
} 