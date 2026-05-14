package com.bookstore.frontend.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class BookCardController {

    @FXML private VBox rootCard;
    @FXML private ImageView imgCover;
    @FXML private Label lblTitle;
    @FXML private Label lblAuthor;
    @FXML private Label lblPrice;
    @FXML private Button btnAdd;

    private Runnable onCardClicked;
    private Runnable onAddClicked;

    @FXML
    public void initialize() {
        rootCard.setOnMouseClicked(event -> {
            if (onCardClicked != null) {
                onCardClicked.run();
            }
        });
    }

    @FXML
    private void handleAddToCart(ActionEvent event) {
        event.consume();
        if (onAddClicked != null) {
            onAddClicked.run();
        }
    }

    public void setBookData(String title, String author, String price, String imageUrl) {
        lblTitle.setText(title);
        lblAuthor.setText(author);
        lblPrice.setText(price);

        try {
            if (imageUrl != null && !imageUrl.isBlank()) {
                Image image = new Image(imageUrl, true);
                imgCover.setImage(image);
            } else {
                imgCover.setImage(null);
            }
        } catch (Exception e) {
            System.err.println("Error loading Cloudinary images for books" + title + ": " + imageUrl);
        }
    }

    public void setCallbacks(Runnable onCardClicked, Runnable onAddClicked) {
        this.onCardClicked = onCardClicked;
        this.onAddClicked = onAddClicked;
    }
}