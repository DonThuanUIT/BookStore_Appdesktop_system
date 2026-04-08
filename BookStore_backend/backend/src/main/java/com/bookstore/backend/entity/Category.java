package com.bookstore.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
    @Entity
    @Table(name = "categories")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class Category extends BaseEntity {
        @Column(nullable = false, length = 100)
        private String name;
    }

