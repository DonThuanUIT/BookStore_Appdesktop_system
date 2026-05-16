package com.bookstore.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "imports")
@Getter
@Setter
public class Import extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "staff_id")
    private AppUser staff;
    private Double totalCost;

    @Column(name = "import_date", updatable = false)
    private LocalDateTime importDate;

    @PrePersist
    public void prePersist() {
        if (importDate == null) {
            importDate = LocalDateTime.now();
        }
    }
}
