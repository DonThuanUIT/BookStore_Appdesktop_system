package com.bookstore.backend.entity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {
    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    private BigDecimal totalAmount;
    private BigDecimal discount;
    private BigDecimal finalAmount;
    @Column(nullable = false)
    private String status;
}
