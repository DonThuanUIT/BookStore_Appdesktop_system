package com.bookstore.frontend.controller;

import com.bookstore.frontend.model.BookModel;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class BookFormController {

    @FXML private Label lblFormTitle;
    @FXML private ImageView imgCover;
    @FXML private TextField txtTitle;
    @FXML private ComboBox<String> cbAuthor;
    @FXML private Button btnSave;

    private BookModel currentBook;
    private File selectedImageFile;
    private boolean saveClicked = false;

    @FXML
    public void initialize() {
        // TODO: Chuẩn bị thay thế bằng dữ liệu gọi từ API
        cbAuthor.getItems().addAll("Carlos Ruiz Zafón", "Nguyễn Nhật Ánh", "M. Scott Peck", "F. Scott Fitzgerald", "Unknown Author");
    }

    public void setBook(BookModel book, boolean isEdit) {
        this.currentBook = book;

        if (isEdit) {
            lblFormTitle.setText("Edit Book Details");
            btnSave.setText("Update Book");

            txtTitle.setText(book.getTitle());
            cbAuthor.setValue(book.getAuthorName());

            if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
                try {
                    imgCover.setImage(new Image(book.getImageUrl(), true));
                } catch (Exception e) {
                    System.err.println("Không thể load ảnh từ URL: " + book.getImageUrl());
                }
            }
        } else {
            lblFormTitle.setText("Add New Book");
            btnSave.setText("Save Book");
        }
    }

    @FXML
    private void handleChangeImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Book Cover Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        Stage stage = (Stage) btnSave.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            this.selectedImageFile = file;
            Image image = new Image(file.toURI().toString());
            imgCover.setImage(image);
        }
    }

    @FXML
    private void handleSave() {
        currentBook.setTitle(txtTitle.getText());
        currentBook.setAuthorName(cbAuthor.getValue());

        this.saveClicked = true;
        closeDialog();
    }

    @FXML
    private void handleCancel() {
        this.saveClicked = false;
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    public File getSelectedImageFile() {
        return selectedImageFile;
    }
}