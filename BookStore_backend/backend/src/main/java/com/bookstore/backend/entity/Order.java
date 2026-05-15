package com.bookstore.backend.entity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {
    @ManyToOne (fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;
    private BigDecimal totalAmount;
    private BigDecimal discount;
    private BigDecimal finalAmount;
    @Column(nullable = false)
    private String status;

    @Column(name = "order_date", updatable = false)
    private LocalDateTime orderDate;

    @PrePersist
    public void prePersist() {
        if (orderDate == null) {
            orderDate = LocalDateTime.now();
        }
    }
}
