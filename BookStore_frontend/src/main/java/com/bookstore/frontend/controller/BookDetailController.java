package com.bookstore.frontend.controller;

import com.bookstore.frontend.model.BookModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class BookDetailController {
    @FXML private ImageView imgLargePreview;
    @FXML private Label lblTitle, lblAuthor, lblDescription, lblPrice;
    @FXML private Label lblPublisher;   // Cần thêm fx:id này vào FXML
    @FXML private Label lblCategories;  // Cần thêm fx:id này vào FXML

    private BookModel currentBook;

    public void setBookData(BookModel book) {
        this.currentBook = book;

        lblTitle.setText(book.getTitle());
        lblAuthor.setText("By " + book.getAuthorName());

        // Hiển thị thông tin chi tiết từ DB
        if (lblPublisher != null) {
            lblPublisher.setText("Publisher: " + (book.getPublisherName() != null ? book.getPublisherName() : "N/A"));
        }
        if (lblCategories != null) {
            lblCategories.setText("Tags: " + book.getFormattedCategories());
        }

        lblPrice.setText(String.format("$%.2f", book.getPrice()));

        if (book.getDescription() != null && !book.getDescription().isEmpty()) {
            lblDescription.setText(book.getDescription());
        } else {
            lblDescription.setText("No description available.");
        }

        if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
            imgLargePreview.setImage(new Image(book.getImageUrl(), true));
        }
    }

    @FXML
    private void onAddToCart() {
        System.out.println("Added to cart: " + currentBook.getTitle());
    }
}