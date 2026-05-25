package com.bookstore.frontend.controller.strategy;

import com.bookstore.frontend.controller.BookFormController;
import com.bookstore.frontend.interactor.InventoryInteractor;
import com.bookstore.frontend.model.BookModel;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class EditBookStrategy implements BookFormStrategy {

    private final InventoryInteractor interactor;

    public EditBookStrategy(InventoryInteractor interactor) {
        this.interactor = interactor;
    }

    @Override
    public void setupUI(BookFormController controller, BookModel book) {
        controller.getLblFormTitle().setText("Cập nhật thông tin Sách");
        controller.getBtnSave().setText("Lưu thay đổi");

        controller.getTxtPrice().setText(String.format("%.0f", book.getPrice() != null ? book.getPrice() : 0.0));
        controller.getTxtPrice().setEditable(false);
        controller.getTxtPrice().setFocusTraversable(false);
        controller.getTxtPrice().setStyle("-fx-opacity: 0.6; -fx-background-color: #2a2d36; -fx-cursor: default;");

        controller.getTxtQuantity().setText(String.valueOf(book.getQuantity() != null ? book.getQuantity() : 0));
        controller.getTxtQuantity().setEditable(false);
        controller.getTxtQuantity().setFocusTraversable(false);
        controller.getTxtQuantity().setStyle("-fx-opacity: 0.6; -fx-background-color: #2a2d36; -fx-cursor: default;");
    }

    @Override
    public CompletableFuture<Boolean> handleSave(BookModel book, File imageFile) {
        return interactor.updateBook(book, imageFile);
    }
}