package com.bookstore.frontend.model;

import com.bookstore.frontend.model.dto.CartItemDTO;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class CartModel {
    private final ObservableList<CartItemDTO> items = FXCollections.observableArrayList();
    private final DoubleProperty totalPrice = new SimpleDoubleProperty(0.0);
    private final IntegerProperty totalQuantity = new SimpleIntegerProperty(0);

    public CartModel() {
        items.addListener((ListChangeListener<CartItemDTO>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (CartItemDTO added : c.getAddedSubList()) {
                        added.quantityProperty().addListener((obs, ov, nv) -> refreshAggregates());
                    }
                }
            }
            refreshAggregates();
        });
    }

    public ObservableList<CartItemDTO> getItems() { return items; }

    public DoubleProperty totalPriceProperty() { return totalPrice; }
    public void setTotalPrice(double total) { this.totalPrice.set(total); }

    public IntegerProperty totalQuantityProperty() {
        return totalQuantity;
    }

    public void refreshAggregates() {
        double total = items.stream().mapToDouble(CartItemDTO::getSubtotal).sum();
        totalPrice.set(total);
        int units = items.stream().mapToInt(CartItemDTO::getQuantity).sum();
        totalQuantity.set(units);
    }

    /**
     * Thêm sách vào giỏ: cùng {@code id} (hoặc cùng title nếu chưa có id) thì tăng số lượng.
     */
    public void addBook(BookModel book, int qty) {
        if (book == null || qty <= 0) {
            return;
        }
        Long id = book.getId();
        if (id != null) {
            for (CartItemDTO existing : items) {
                if (id.equals(existing.getBook().getId())) {
                    existing.setQuantity(existing.getQuantity() + qty);
                    return;
                }
            }
        } else {
            String title = book.getTitle();
            if (title != null) {
                for (CartItemDTO existing : items) {
                    if (title.equals(existing.getBook().getTitle())) {
                        existing.setQuantity(existing.getQuantity() + qty);
                        return;
                    }
                }
            }
        }
        items.add(new CartItemDTO(book, qty));
    }

    public void removeItem(CartItemDTO item) {
        if (item != null) {
            items.remove(item);
        }
    }

    public void clearCart() {
        this.getItems().clear(); // getItems() trả về ObservableList nên clear() sẽ tự cập nhật UI
    }
}
