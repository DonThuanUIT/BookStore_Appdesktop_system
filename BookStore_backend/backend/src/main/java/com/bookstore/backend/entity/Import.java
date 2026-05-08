package com.bookstore.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "imports")
@Getter
@Setter
public class Import extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "staff_id")
    private AppUser staff;
    private Double totalCost;
}
