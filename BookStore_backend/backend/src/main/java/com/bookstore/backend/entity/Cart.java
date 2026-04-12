package com.bookstore.backend.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "carts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Cart extends BaseEntity {
    @OneToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}