package com.bookstore.frontend.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ShopModel {
    private final ObservableList<BookModel> books = FXCollections.observableArrayList();

    public ObservableList<BookModel> getBooks() {
        return books;
    }
}