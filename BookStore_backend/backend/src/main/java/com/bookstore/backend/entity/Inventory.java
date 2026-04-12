package com.bookstore.backend.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name = "inventories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Inventory extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;
    private Integer stock;
}