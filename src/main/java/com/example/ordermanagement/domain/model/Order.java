package com.example.ordermanagement.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {
    @Column(name = "order_number", nullable = false, unique = true)
    private UUID orderNumber;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    public Order addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
        return this;
    }

    public Order removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
        return this;
    }
} 