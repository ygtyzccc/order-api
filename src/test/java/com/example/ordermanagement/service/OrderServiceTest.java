package com.example.ordermanagement.service;

import com.example.ordermanagement.domain.model.Order;
import com.example.ordermanagement.domain.model.OrderStatus;
import com.example.ordermanagement.domain.model.Product;
import com.example.ordermanagement.domain.model.User;
import com.example.ordermanagement.domain.repository.OrderRepository;
import com.example.ordermanagement.domain.repository.ProductRepository;
import com.example.ordermanagement.domain.repository.UserRepository;
import com.example.ordermanagement.dto.CreateOrderRequest;
import com.example.ordermanagement.exception.InsufficientStockException;
import com.example.ordermanagement.exception.ResourceNotFoundException;
import com.example.ordermanagement.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WebhookService webhookService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User testUser;
    private Product testProduct;
    private Order testOrder;
    private UUID orderNumber;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(BigDecimal.valueOf(100))
                .stockQuantity(10)
                .build();

        orderNumber = UUID.randomUUID();
        testOrder = Order.builder()
                .orderNumber(orderNumber)
                .user(testUser)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(100))
                .build();
    }

    @Test
    void createOrder_Success() {
        // Arrange
        var request = CreateOrderRequest.builder()
                .userId(testUser.getId())
                .items(List.of(new CreateOrderRequest.OrderItemRequest(testProduct.getId(), 2)))
                .build();

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(productRepository.findWithLockById(testProduct.getId())).thenReturn(Optional.of(testProduct));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        var result = orderService.createOrder(request);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getUserId());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        verify(webhookService).notifyOrderCreated(any(Order.class));
    }

    @Test
    void createOrder_UserNotFound() {
        // Arrange
        var request = CreateOrderRequest.builder()
                .userId(999L)
                .items(List.of(new CreateOrderRequest.OrderItemRequest(testProduct.getId(), 2)))
                .build();

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(request));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_InsufficientStock() {
        // Arrange
        var request = CreateOrderRequest.builder()
                .userId(testUser.getId())
                .items(List.of(new CreateOrderRequest.OrderItemRequest(testProduct.getId(), 20)))
                .build();

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(productRepository.findWithLockById(testProduct.getId())).thenReturn(Optional.of(testProduct));

        // Act & Assert
        assertThrows(InsufficientStockException.class, () -> orderService.createOrder(request));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void cancelOrder_Success() {
        // Arrange
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        orderService.cancelOrder(orderNumber);

        // Assert
        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
        verify(webhookService).notifyOrderCancelled(testOrder);
    }

    @Test
    void cancelOrder_OrderNotFound() {
        // Arrange
        when(orderRepository.findByOrderNumber(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderService.cancelOrder(UUID.randomUUID()));
        verify(webhookService, never()).notifyOrderCancelled(any());
    }

    @Test
    void getOrder_Success() {
        // Arrange
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.of(testOrder));

        // Act
        var result = orderService.getOrder(orderNumber);

        // Assert
        assertNotNull(result);
        assertEquals(orderNumber, result.getOrderNumber());
        assertEquals(testUser.getId(), result.getUserId());
    }

    @Test
    void getUserOrders_Success() {
        // Arrange
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId()))
                .thenReturn(List.of(testOrder));

        // Act
        var result = orderService.getUserOrders(testUser.getId());

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testUser.getId(), result.get(0).getUserId());
    }
} 