package com.bookstore.backend.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
@Entity @Table(name = "publishers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Publisher extends BaseEntity {
    @Column(nullable = false, length = 100)
    private String name;
}
