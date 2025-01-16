package com.example.ordermanagement.domain.specification;

import com.example.ordermanagement.domain.model.Order;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

public class OrderSpecifications {

    public static Specification<Order> byUserId(Long userId) {
        return (root, query, cb) -> {
            if (userId == null) return null;
            return cb.equal(root.get("user").get("id"), userId);
        };
    }

    public static Specification<Order> betweenDates(LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            if (startDate == null || endDate == null) return null;

            var start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            var end = endDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();

            return cb.between(root.get("createdAt"), start, end);
        };
    }

    public static Specification<Order> byStatus(String status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }
} 