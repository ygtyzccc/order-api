package com.example.ordermanagement.service.impl;

import com.example.ordermanagement.domain.model.Order;
import com.example.ordermanagement.domain.model.OrderItem;
import com.example.ordermanagement.domain.model.OrderStatus;
import com.example.ordermanagement.domain.model.Product;
import com.example.ordermanagement.domain.repository.OrderRepository;
import com.example.ordermanagement.domain.repository.ProductRepository;
import com.example.ordermanagement.domain.repository.UserRepository;
import com.example.ordermanagement.domain.specification.OrderSpecifications;
import com.example.ordermanagement.dto.CreateOrderRequest;
import com.example.ordermanagement.dto.OrderDto;
import com.example.ordermanagement.dto.OrderItemDto;
import com.example.ordermanagement.dto.OrderSearchRequest;
import com.example.ordermanagement.exception.InsufficientStockException;
import com.example.ordermanagement.exception.ResourceNotFoundException;
import com.example.ordermanagement.mapper.OrderMapper;
import com.example.ordermanagement.service.OrderService;
import com.example.ordermanagement.service.WebhookService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final WebhookService webhookService;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderDto createOrder(CreateOrderRequest request) {
        var user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        var order = Order.builder()
                .orderNumber(UUID.randomUUID())
                .user(user)
                .status(OrderStatus.PENDING)
                .build();

        user.addOrder(order);

        processOrderItems(order, request.getItems());
        order.setTotalAmount(calculateTotalAmount(order.getItems()));

        var savedOrder = orderRepository.save(order);
        webhookService.notifyOrderCreated(savedOrder);

        return orderMapper.toDto(savedOrder);
    }

    private void processOrderItems(Order order, List<CreateOrderRequest.OrderItemRequest> itemRequests) {
        itemRequests.forEach(itemRequest -> {
            var product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found"));

            validateAndUpdateStock(product, itemRequest.getQuantity());

            var orderItem = createOrderItem(order, product, itemRequest.getQuantity());
            order.addItem(orderItem);
        });
    }

    private void validateAndUpdateStock(Product product, Integer requestedQuantity) {
        if (product.getStockQuantity() < requestedQuantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for product: " + product.getName());
        }
        product.setStockQuantity(product.getStockQuantity() - requestedQuantity);
        productRepository.save(product);
    }

    private OrderItem createOrderItem(Order order, Product product, Integer quantity) {
        return OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(quantity)
                .unitPrice(product.getPrice())
                .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(quantity)))
                .build();
    }

    private BigDecimal calculateTotalAmount(List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrder(UUID orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .map(this::mapToDto)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
    }

    @Override
    @Transactional
    public void cancelOrder(UUID orderNumber) {
        var order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order cannot be cancelled in current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);

        // Restore product stock
        order.getItems().forEach(item -> {
            var product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        });

        orderRepository.save(order);
        webhookService.notifyOrderCancelled(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> searchOrders(OrderSearchRequest request) {
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        // Verify user exists
        userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Specification<Order> spec = Specification.where(null);

        // Add user filter
        spec = spec.and(OrderSpecifications.byUserId(request.getUserId()));

        // Add date range filter if provided
        if (request.getStartDate() != null && request.getEndDate() != null) {
            spec = spec.and(OrderSpecifications.betweenDates(request.getStartDate(), request.getEndDate()));
        }

        // Add status filter if provided
        if (request.getStatus() != null) {
            spec = spec.and(OrderSpecifications.byStatus(request.getStatus()));
        }

        // Create pageable for pagination and sorting
        Pageable pageable = PageRequest.of(
                request.getPage() != null ? request.getPage() : 0,
                request.getSize() != null ? request.getSize() : 20,
                Sort.by(request.getSortDirection() != null && request.getSortDirection().equalsIgnoreCase("desc")
                                ? Sort.Direction.DESC
                                : Sort.Direction.ASC,
                        request.getSortBy() != null ? request.getSortBy() : "createdAt")
        );

        return orderRepository.findAll(spec, pageable)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private OrderDto mapToDto(Order order) {
        return OrderDto.builder()
                .orderNumber(order.getOrderNumber())
                .userId(order.getUser().getId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .items(order.getItems().stream()
                        .map(this::mapToDto)
                        .collect(Collectors.toList()))
                .createdAt(order.getCreatedAt())
                .build();
    }

    private OrderItemDto mapToDto(OrderItem item) {
        return OrderItemDto.builder()
                .productId(item.getProduct().getId())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }
} 