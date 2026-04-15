package com.bookstore.frontend.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class CartItemDTO {
    private final BookDTO book;
    private final IntegerProperty quantity;

    public CartItemDTO(BookDTO book, int quantity) {
        this.book = book;
        this.quantity = new SimpleIntegerProperty(quantity);
    }

    public BookDTO getBook() { return book; }

    public int getQuantity() { return quantity.get(); }
    public void setQuantity(int quantity) { this.quantity.set(quantity); }
    public IntegerProperty quantityProperty() { return quantity; }

    public double getSubtotal() {
        String priceStr = book.getPrice().replaceAll("[^0-9]", "");
        return Double.parseDouble(priceStr) * getQuantity();
    }
}
