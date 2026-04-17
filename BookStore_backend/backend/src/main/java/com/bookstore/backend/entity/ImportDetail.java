package com.bookstore.backend.entity;

import com.bookstore.backend.listener.ImportDetailQuantityListener;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@EntityListeners(ImportDetailQuantityListener.class)
@Table(name = "import_details")
@Getter
@Setter
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
