package com.example.ordermanagement.controller;

import com.example.ordermanagement.config.TestContainersConfig;
import com.example.ordermanagement.domain.model.Product;
import com.example.ordermanagement.domain.model.User;
import com.example.ordermanagement.domain.repository.ProductRepository;
import com.example.ordermanagement.domain.repository.UserRepository;
import com.example.ordermanagement.dto.CreateOrderRequest;
import com.example.ordermanagement.dto.OrderDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class OrderControllerIntegrationTest extends TestContainersConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    private User testUser;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        productRepository.deleteAll();

        testUser = userRepository.save(User.builder()
                .username("testuser")
                .email("test@example.com")
                .build());

        testProduct = productRepository.save(Product.builder()
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(100))
                .stockQuantity(10)
                .build());
    }

    @Test
    void createOrder_Success() throws Exception {
        var request = CreateOrderRequest.builder()
                .userId(testUser.getId())
                .items(List.of(new CreateOrderRequest.OrderItemRequest(testProduct.getId(), 2)))
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.totalAmount").value("200.00"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createOrder_InsufficientStock() throws Exception {
        var request = CreateOrderRequest.builder()
                .userId(testUser.getId())
                .items(List.of(new CreateOrderRequest.OrderItemRequest(testProduct.getId(), 20)))
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getUserOrders_Success() throws Exception {
        // First create an order
        var request = CreateOrderRequest.builder()
                .userId(testUser.getId())
                .items(List.of(new CreateOrderRequest.OrderItemRequest(testProduct.getId(), 1)))
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Then get user orders
        mockMvc.perform(get("/api/v1/orders/user/{userId}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(testUser.getId()))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void cancelOrder_Success() throws Exception {
        // First create an order
        var request = CreateOrderRequest.builder()
                .userId(testUser.getId())
                .items(List.of(new CreateOrderRequest.OrderItemRequest(testProduct.getId(), 2)))
                .build();

        var result = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        var orderDto = objectMapper.readValue(result.getResponse().getContentAsString(), OrderDto.class);

        // Then cancel it
        mockMvc.perform(post("/api/v1/orders/{orderNumber}/cancel", orderDto.getOrderNumber()))
                .andExpect(status().isNoContent());

        // Verify the order status is updated
        mockMvc.perform(get("/api/v1/orders/{orderNumber}", orderDto.getOrderNumber()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void createOrder_UserNotFound() throws Exception {
        var request = CreateOrderRequest.builder()
                .userId(999L)
                .items(List.of(new CreateOrderRequest.OrderItemRequest(testProduct.getId(), 1)))
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createOrder_ProductNotFound() throws Exception {
        var request = CreateOrderRequest.builder()
                .userId(testUser.getId())
                .items(List.of(new CreateOrderRequest.OrderItemRequest(999L, 1)))
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserOrdersByDate_Success() throws Exception {
        // Create an order
        var request = CreateOrderRequest.builder()
                .userId(testUser.getId())
                .items(List.of(new CreateOrderRequest.OrderItemRequest(testProduct.getId(), 1)))
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Get orders by date range
        LocalDate today = LocalDate.now();
        mockMvc.perform(get("/api/v1/orders/user/{userId}", testUser.getId())
                        .param("startDate", today.minusDays(1).toString())
                        .param("endDate", today.plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(testUser.getId()))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }
} 