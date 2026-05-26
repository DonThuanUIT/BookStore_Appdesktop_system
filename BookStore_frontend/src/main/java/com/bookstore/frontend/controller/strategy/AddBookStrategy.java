package com.bookstore.frontend.controller.strategy;

import com.bookstore.frontend.controller.BookFormController;
import com.bookstore.frontend.interactor.InventoryInteractor;
import com.bookstore.frontend.model.BookModel;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class AddBookStrategy implements BookFormStrategy {

    private final InventoryInteractor interactor;

    public AddBookStrategy(InventoryInteractor interactor) {
        this.interactor = interactor;
    }

    @Override
    public void setupUI(BookFormController controller, BookModel book) {
        controller.getLblFormTitle().setText("Thêm Sách Mới");
        controller.getBtnSave().setText("Thêm Sách");

        controller.getTxtPrice().setEditable(true);
        controller.getTxtPrice().setFocusTraversable(true);
        controller.getTxtPrice().setStyle("-fx-text-fill: white; -fx-background-color: transparent;");

        controller.getTxtQuantity().setEditable(true);
        controller.getTxtQuantity().setFocusTraversable(true);
        controller.getTxtQuantity().setStyle("-fx-text-fill: white; -fx-background-color: transparent;");
    }

    @Override
    public CompletableFuture<Boolean> handleSave(BookModel book, File imageFile) {
        return interactor.createBook(book, imageFile);
    }
}