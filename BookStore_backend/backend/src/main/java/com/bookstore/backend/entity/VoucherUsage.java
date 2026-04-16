package com.bookstore.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "voucher_usages")
public class VoucherUsage extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    private LocalDateTime usedAt = LocalDateTime.now();
}