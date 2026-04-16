package com.bookstore.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "imports")
public class Import extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "staff_id")
    private AppUser staff;
    private Double totalCost;
}