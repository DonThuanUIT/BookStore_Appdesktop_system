package com.bookstore.frontend.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CartModel {
    private final ObservableList<CartItemDTO> items = FXCollections.observableArrayList();
    private final DoubleProperty totalPrice = new SimpleDoubleProperty(0.0);

    public ObservableList<CartItemDTO> getItems() { return items; }

    public DoubleProperty totalPriceProperty() { return totalPrice; }
    public void setTotalPrice(double total) { this.totalPrice.set(total); }
}
