package com.bookstore.frontend.controller;

import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.service.api.ApiClient;
import com.bookstore.frontend.service.api.BookApiService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
    private boolean isEditMode = false; // Cờ phân biệt trạng thái

    private final BookApiService bookApiService = new BookApiService();

    @FXML
    public void initialize() {
        // mock data, thay thế bằng api sau.
        cbAuthor.getItems().addAll("Nguyễn Nhật Ánh", "Carlos Ruiz Zafón", "Unknown Author");
    }

    public void setBook(BookModel book, boolean isEdit) {
        this.currentBook = book;
        this.isEditMode = isEdit;

        if (isEdit) {
            lblFormTitle.setText("Edit Book Details");
            btnSave.setText("Update Book");
            txtTitle.setText(book.getTitle());
            cbAuthor.setValue(book.getAuthorName());
            if (book.getImageUrl() != null) imgCover.setImage(new Image(book.getImageUrl(), true));
        } else {
            lblFormTitle.setText("Add New Book");
            btnSave.setText("Save Book");
        }
    }

    @FXML
    private void handleChangeImage() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = fc.showOpenDialog(btnSave.getScene().getWindow());
        if (file != null) {
            this.selectedImageFile = file;
            imgCover.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void handleSave() {
        btnSave.setDisable(true);
        btnSave.setText("Processing...");

        currentBook.setTitle(txtTitle.getText());
        currentBook.setAuthorName(cbAuthor.getValue());

        if (selectedImageFile != null) {
            uploadAndThenSave();
        } else {
            executeFinalSave();
        }
    }

    private void uploadAndThenSave() {
        try {
            ApiClient.getInstance().uploadFile("/books/upload", selectedImageFile).thenAccept(res -> {
                if (res.statusCode() < 300) {
                    currentBook.setImageUrl(res.body()); // Gán URL trả về từ server
                    executeFinalSave();
                } else {
                    showError("Upload failed!");
                }
            });
        } catch (Exception e) { showError("IO Error!"); }
    }

    private void executeFinalSave() {
        var future = isEditMode ? bookApiService.updateBook(currentBook.getId(), currentBook) : bookApiService.createBook(currentBook);

        future.thenAccept(success -> Platform.runLater(() -> {
            if (success) {
                this.saveClicked = true;
                ((Stage) btnSave.getScene().getWindow()).close();
            } else {
                showError("Save failed!");
            }
        }));
    }

    private void showError(String msg) {
        Platform.runLater(() -> {
            btnSave.setDisable(false);
            btnSave.setText(isEditMode ? "Update Book" : "Save Book");
            new Alert(Alert.AlertType.ERROR, msg).show();
        });
    }

    @FXML void handleCancel() { ((Stage) btnSave.getScene().getWindow()).close(); }
    public boolean isSaveClicked() { return saveClicked; }
}