package com.bookstore.frontend.model.dto;

import com.bookstore.frontend.model.BookModel;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class CartItemDTO {
    // Đã đổi từ BookDTO sang BookModel
    private final BookModel book;
    private final IntegerProperty quantity;

    public CartItemDTO(BookModel book, int quantity) {
        this.book = book;
        this.quantity = new SimpleIntegerProperty(quantity);
    }

    public BookModel getBook() {
        return book;
    }

    public int getQuantity() {
        return quantity.get();
    }

    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
    }

    public IntegerProperty quantityProperty() {
        return quantity;
    }

    public double getSubtotal() {
        if (book == null || book.getPrice() == null) {
            return 0.0;
        }
        return book.getPrice() * getQuantity();
    }
}