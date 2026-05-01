package com.bookstore.frontend.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class ShopModel {
    private final ObservableList<BookModel> books = FXCollections.observableArrayList();

    public ObservableList<BookModel> getBooks() {
        return books;
    }

    public void setBooks(List<BookModel> bookList) {
        this.books.setAll(bookList);
    }

    /**
     * Phương thức thêm sách vào danh sách hiện tại (dùng cho Lazy Loading)
     */
    public void addBooks(List<BookModel> newBooks) {
        this.books.addAll(newBooks);
    }
}