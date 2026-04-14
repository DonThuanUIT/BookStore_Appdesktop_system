package com.bookstore.frontend.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ShopModel {
    private final ObservableList<BookDTO> books = FXCollections.observableArrayList();

    public ObservableList<BookDTO> getBooks() {
        return books;
    }
}
