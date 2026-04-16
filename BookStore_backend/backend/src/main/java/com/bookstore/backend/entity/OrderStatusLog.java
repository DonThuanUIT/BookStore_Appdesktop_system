package com.bookstore.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_status_logs")
public class OrderStatusLog extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    private String status;
    private LocalDateTime changedAt = LocalDateTime.now();
}
