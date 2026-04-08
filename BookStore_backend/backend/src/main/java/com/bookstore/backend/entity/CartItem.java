package com.bookstore.backend.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name = "cart_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CartItem extends BaseEntity {
    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;
    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;
    @Column (nullable = false)
    private Integer quantity;
}