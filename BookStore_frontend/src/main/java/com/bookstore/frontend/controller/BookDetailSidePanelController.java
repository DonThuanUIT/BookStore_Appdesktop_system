package com.bookstore.frontend.controller;

import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.util.CartStore;
import com.bookstore.frontend.utils.AlertUtils;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class BookDetailSidePanelController {
    @FXML private VBox rootPanel;
    @FXML private ImageView imgCover;
    @FXML private Label lblTitle;
    @FXML private Label lblAuthor;
    @FXML private Label lblPrice;
    @FXML private Label lblPublisher;
    @FXML private Label lblCategories;
    @FXML private Label lblDescription;

    private BookModel currentBook;

    @FXML
    public void initialize() {
        rootPanel.setVisible(false);
        rootPanel.setManaged(false);
    }

    public void setBookDetailDataAndShow(BookModel book) {
        this.currentBook = book;
        lblTitle.setText(book.getTitle());
        lblAuthor.setText("By " + book.getAuthorNames());
        lblPrice.setText(String.format("%,.0f đ", book.getPrice()));

        if (lblPublisher != null)
            lblPublisher.setText("Publisher: " + (book.getPublisherName() != null ? book.getPublisherName() : "N/A"));
        if (lblCategories != null)
            lblCategories.setText("Tags: " + book.getFormattedCategories());

        if (lblDescription != null) {
            if (book.getDescription() != null && !book.getDescription().isBlank()) {
                lblDescription.setText(book.getDescription());
            } else {
                lblDescription.setText("No description available.");
            }
        }

        try {
            if (book.getImageUrl() != null && !book.getImageUrl().isBlank()) {
                Image image = new Image(book.getImageUrl(), true);
                imgCover.setImage(image);
            } else {
                imgCover.setImage(null);
            }
        } catch (Exception e) {
            System.err.println("SidePanel - Lỗi nạp ảnh Cloudinary: " + book.getImageUrl());
        }

        rootPanel.setVisible(true);
        rootPanel.setManaged(true);

        TranslateTransition translate = new TranslateTransition(Duration.millis(350), rootPanel);
        translate.setFromX(100);
        translate.setToX(0);

        FadeTransition fade = new FadeTransition(Duration.millis(350), rootPanel);
        fade.setFromValue(0);
        fade.setToValue(1);

        ParallelTransition slideIn = new ParallelTransition(translate, fade);
        slideIn.play();
    }


    @FXML
    public void handleClose() {

        TranslateTransition translate = new TranslateTransition(Duration.millis(250), rootPanel);
        translate.setFromX(0);
        translate.setToX(100);

        FadeTransition fade = new FadeTransition(Duration.millis(250), rootPanel);
        fade.setFromValue(1);
        fade.setToValue(0);

        ParallelTransition slideOut = new ParallelTransition(translate, fade);

        slideOut.setOnFinished(event -> {
            rootPanel.setVisible(false);
            rootPanel.setManaged(false);
        });

        slideOut.play();
    }

    @FXML
    public void handleAddToCart() {
        if (currentBook == null) {
            return;
        }
        AlertUtils.promptQuantityForCart(currentBook.getTitle())
                .ifPresent(qty -> CartStore.getInstance().addBook(currentBook, qty));
    }
}
