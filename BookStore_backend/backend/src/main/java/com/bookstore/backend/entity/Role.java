package com.bookstore.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {
    @Column(name = "name", unique = true, nullable = false, length = 50)
    private String name;
    @Column(name = "description", length = 255)
    private String description;
    public String getName() {
        return this.name;
    }
}
