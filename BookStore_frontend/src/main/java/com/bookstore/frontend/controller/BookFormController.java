package com.bookstore.frontend.controller;

import com.bookstore.frontend.components.TagInputField;
import com.bookstore.frontend.interactor.InventoryInteractor;
import com.bookstore.frontend.model.BookModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookFormController {

    @FXML private Label lblFormTitle;
    @FXML private ImageView imgCover;
    @FXML private TextField txtTitle;

    // NHÚNG COMPONENT SIÊU GỌN GÀNG
    @FXML private TagInputField tagInputAuthor;
    @FXML private TagInputField tagInputCategory;

    @FXML private ComboBox<String> cbPublisher;
    @FXML private TextField txtPrice;
    @FXML private TextField txtQuantity;
    @FXML private TextArea txtDescription;
    @FXML private Button btnSave;

    private BookModel currentBook;
    private File selectedImageFile;
    private boolean saveClicked = false;
    private boolean isEditMode = false;
    private InventoryInteractor interactor;

    // Giả lập Dữ liệu gốc từ Backend (Map giữa Tên hiển thị và ID trong Database)
    private final Map<String, Long> masterAuthors = new HashMap<>();
    private final Map<String, Long> masterCategories = new HashMap<>();
    private final Map<String, Long> masterPublishers = new HashMap<>();

    @FXML
    public void initialize() {
        // MOCK DATA: Chờ tích hợp API Master Data
        masterAuthors.put("Nguyễn Nhật Ánh", 1L);
        masterAuthors.put("Carlos Ruiz Zafón", 2L);
        masterAuthors.put("J.K. Rowling", 3L);
        masterAuthors.put("Tô Hoài", 4L);

        masterCategories.put("Văn học", 1L);
        masterCategories.put("Tiểu thuyết", 2L);
        masterCategories.put("Khoa học viễn tưởng", 3L);
        masterCategories.put("Thiếu nhi", 4L);

        masterPublishers.put("NXB Trẻ", 1L);
        masterPublishers.put("Nhã Nam", 2L);
        masterPublishers.put("Kim Đồng", 3L);

        cbPublisher.getItems().addAll(masterPublishers.keySet());

        // Đổ dữ liệu gợi ý vào các Component
        tagInputAuthor.setPromptText("Gõ tên tác giả...");
        tagInputAuthor.setSuggestionsPool(new ArrayList<>(masterAuthors.keySet()));

        tagInputCategory.setPromptText("Gõ tên thể loại...");
        tagInputCategory.setSuggestionsPool(new ArrayList<>(masterCategories.keySet()));
    }

    public void setInteractor(InventoryInteractor interactor) {
        this.interactor = interactor;
    }

    public void setBook(BookModel book, boolean isEdit) {
        this.currentBook = book;
        this.isEditMode = isEdit;

        if (isEdit) {
            lblFormTitle.setText("Cập nhật thông tin Sách");
            btnSave.setText("Lưu thay đổi");

            // Đổ dữ liệu text cơ bản
            txtTitle.setText(book.getTitle());
            txtPrice.setText(String.format("%.0f", book.getPrice() != null ? book.getPrice() : 0.0));
            txtQuantity.setText(String.valueOf(book.getQuantity() != null ? book.getQuantity() : 0));
            txtDescription.setText(book.getDescription());
            cbPublisher.setValue(book.getPublisherName());

            if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
                try {
                    imgCover.setImage(new Image(book.getImageUrl(), true));
                } catch (Exception ignored) {}
            }

            // RA LỆNH CHO COMPONENT RENDER GIAO DIỆN CHIP RẤT SẠCH SẼ
            tagInputAuthor.setTags(book.getAuthorNames());
            tagInputCategory.setTags(book.getCategoryNames());
        } else {
            lblFormTitle.setText("Thêm Sách Mới");
            btnSave.setText("Thêm Sách");
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
        if (txtTitle.getText().isEmpty() || cbPublisher.getValue() == null) {
            showError("Vui lòng nhập Tên sách và chọn Nhà xuất bản!");
            return;
        }

        btnSave.setDisable(true);
        btnSave.setText("Đang xử lý...");

        // Cập nhật thuộc tính cơ bản
        currentBook.setTitle(txtTitle.getText());
        currentBook.setDescription(txtDescription.getText());

        try {
            currentBook.setPrice(Double.parseDouble(txtPrice.getText()));
            currentBook.setQuantity(Integer.parseInt(txtQuantity.getText()));
        } catch (NumberFormatException e) {
            showError("Giá bán và Số lượng phải là chữ số!");
            return;
        }

        String pubName = cbPublisher.getValue();
        currentBook.setPublisherName(pubName);
        currentBook.setPublisherId(masterPublishers.getOrDefault(pubName, null));

        // LẤY DỮ LIỆU TỪ COMPONENT ĐỂ ĐÓNG GÓI MODEL (RẤT NHÀN NHÃ)
        List<String> selectedAuthors = tagInputAuthor.getTags();
        List<Long> mappedAuthorIds = new ArrayList<>();
        for (String authorName : selectedAuthors) {
            Long id = masterAuthors.get(authorName);
            mappedAuthorIds.add(id != null ? id : -1L);
        }
        currentBook.setAuthorNames(selectedAuthors);
        currentBook.setAuthorIds(mappedAuthorIds);

        List<String> selectedCategories = tagInputCategory.getTags();
        List<Long> mappedCategoryIds = new ArrayList<>();
        for (String catName : selectedCategories) {
            Long id = masterCategories.get(catName);
            mappedCategoryIds.add(id != null ? id : -1L);
        }
        currentBook.setCategoryNames(selectedCategories);
        currentBook.setCategoryIds(mappedCategoryIds);

        // Đẩy xuống Interactor (Đã được nâng cấp bằng Mapper DTO)
        if (isEditMode) {
            interactor.updateBook(currentBook, selectedImageFile).thenAccept(success -> {
                Platform.runLater(() -> {
                    if (success) {
                        this.saveClicked = true;
                        ((Stage) btnSave.getScene().getWindow()).close();
                    } else {
                        showError("Cập nhật thất bại. Vui lòng kiểm tra Server!");
                    }
                });
            });
        } else {
            // Logic tạo mới sẽ làm sau
        }
    }

    private void showError(String msg) {
        Platform.runLater(() -> {
            btnSave.setDisable(false);
            btnSave.setText(isEditMode ? "Lưu thay đổi" : "Thêm Sách");
            new Alert(Alert.AlertType.ERROR, msg).show();
        });
    }

    @FXML void handleCancel() { ((Stage) btnSave.getScene().getWindow()).close(); }
    public boolean isSaveClicked() { return saveClicked; }
}