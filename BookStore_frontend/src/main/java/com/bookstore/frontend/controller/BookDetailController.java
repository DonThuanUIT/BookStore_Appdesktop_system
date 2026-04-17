package com.bookstore.frontend.controller;

import com.bookstore.frontend.model.BookModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class BookDetailController {
    @FXML private ImageView imgLargePreview;
    @FXML private Label lblTitle, lblAuthor, lblDescription, lblPrice;

    private BookModel currentBook;

    public void setBookData(BookModel book) {
        this.currentBook = book;

        lblTitle.setText(book.getTitle());
        lblAuthor.setText("By " + book.getAuthorName());
        lblPrice.setText(String.format("%,.0fđ", book.getPrice()));

        // Hiển thị mô tả nếu có
        if (book.getDescription() != null && !book.getDescription().isEmpty()) {
            lblDescription.setText(book.getDescription());
        }

        // Tải ảnh chất lượng cao
        if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
            imgLargePreview.setImage(new Image(book.getImageUrl(), true));
        }
    }

    @FXML
    private void onAddToCart() {
        System.out.println("Đã thêm vào giỏ hàng: " + currentBook.getTitle());
        // Logic xử lý giỏ hàng sẽ được thêm ở bước tiếp theo
    }
}