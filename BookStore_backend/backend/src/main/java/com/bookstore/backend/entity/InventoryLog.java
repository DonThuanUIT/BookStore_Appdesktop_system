package com.bookstore.backend.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table (name="inventory_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryLog extends BaseEntity {
    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;
    private Integer changeQuantity;
    @Enumerated(EnumType.STRING)
    private InventoryType type;
    public enum InventoryType {
        IN, OUT
    }
}

