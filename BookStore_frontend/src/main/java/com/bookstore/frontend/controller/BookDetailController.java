package com.bookstore.frontend.controller;

import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.util.CartStore;
import com.bookstore.frontend.utils.AlertUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class BookDetailController {

    @FXML private ImageView imgLargePreview;
    @FXML private Label lblTitle;
    @FXML private Label lblAuthor;
    @FXML private Label lblDescription;
    @FXML private Label lblPrice;

    private BookModel currentBook;
    private static final String DEFAULT_COVER_URL = "https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg";

    public void setBookDetailDataAndShow(BookModel book) {
        if (book == null) return;
        this.currentBook = book;

        lblTitle.setText(book.getTitle());
        lblAuthor.setText("Tác giả: " + book.getAuthorName());

        if (book.getDescription() != null && !book.getDescription().isBlank()) {
            lblDescription.setText(book.getDescription());
        } else {
            lblDescription.setText("No description available.");
        }

        lblPrice.setText(String.format("$%.2f", book.getPrice()));

        String url = (book.getImageUrl() != null && !book.getImageUrl().isBlank())
                ? book.getImageUrl()
                : DEFAULT_COVER_URL;

        try {
            // backgroundLoading = true giúp giao diện mượt mà khi tải ảnh từ internet
            Image image = new Image(url, true);
            imgLargePreview.setImage(image);
        } catch (Exception e) {
            System.err.println("Không thể nạp ảnh bìa chi tiết: " + e.getMessage());
            imgLargePreview.setImage(new Image(DEFAULT_COVER_URL));
        }
    }

    @FXML
    private void onAddToCart() {
        if (currentBook != null) {
            AlertUtils.promptQuantityForCart(currentBook.getTitle())
                    .ifPresent(qty -> CartStore.getInstance().addBook(currentBook, qty));
        }
    }
}