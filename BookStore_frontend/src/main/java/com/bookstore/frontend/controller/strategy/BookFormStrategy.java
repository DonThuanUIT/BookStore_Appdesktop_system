package com.bookstore.frontend.controller.strategy;

import com.bookstore.frontend.controller.BookFormController;
import com.bookstore.frontend.model.BookModel;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public interface BookFormStrategy {

    void setupUI(BookFormController controller, BookModel book);

    CompletableFuture<Boolean> handleSave(BookModel book, File imageFile);
}