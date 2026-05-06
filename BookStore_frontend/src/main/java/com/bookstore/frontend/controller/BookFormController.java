package com.bookstore.frontend.controller;

import com.bookstore.frontend.model.BookModel;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class BookFormController {
    @FXML private Label lblHeader;
    @FXML private TextField txtTitle, txtAuthorName, txtPublisherName, txtPrice, txtQuantity, txtCategoryNames;
    @FXML private Label lblFileName;
    @FXML private ImageView imgPreview;
    @FXML private Button btnSave;

    private File selectedImageFile;
    private boolean saveClicked = false;
    private BookModel book;

    public void setBook(BookModel book, boolean isEdit) {
        this.book = book;
        if (isEdit) {
            lblHeader.setText("Edit Book Details");
            btnSave.setText("Update Book");

            // Đổ dữ liệu từ Model vào UI
            txtTitle.setText(book.getTitle());
            txtAuthorName.setText(book.getAuthorName());
            txtPublisherName.setText(book.getPublisherName());
            txtPrice.setText(book.getPrice() != null ? String.valueOf(book.getPrice()) : "0");
            txtQuantity.setText(book.getQuantity() != null ? String.valueOf(book.getQuantity()) : "0");

            if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
                imgPreview.setImage(new Image(book.getImageUrl(), true));
            }
        }
    }

    @FXML
    private void onSave() {
        try {
            // Cập nhật ngược lại Model từ các TextField
            book.setTitle(txtTitle.getText().trim());
            book.setPublisherName(txtPublisherName.getText().trim());
            book.setPrice(Double.parseDouble(txtPrice.getText()));
            book.setQuantity(Integer.parseInt(txtQuantity.getText()));

            saveClicked = true;
            closeStage();
        } catch (NumberFormatException e) {
            // Bạn có thể dùng AlertUtils ở đây để báo nhập sai định dạng số
            System.err.println("Lỗi: Giá hoặc số lượng phải là số!");
        }
    }

    @FXML
    private void onChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Book Cover Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(btnSave.getScene().getWindow());
        if (file != null) {
            this.selectedImageFile = file;
            lblFileName.setText(file.getName());
            imgPreview.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML private void onCancel() { closeStage(); }
    private void closeStage() { ((Stage) btnSave.getScene().getWindow()).close(); }
    public boolean isSaveClicked() { return saveClicked; }
    public File getSelectedImageFile() { return selectedImageFile; }
}