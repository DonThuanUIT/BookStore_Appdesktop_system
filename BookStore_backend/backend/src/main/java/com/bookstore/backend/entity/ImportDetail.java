package com.bookstore.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "import_details")
public class ImportDetail extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "import_id")
    private Import importOrder;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    private Integer quantity;
    private Double importPrice;
}
