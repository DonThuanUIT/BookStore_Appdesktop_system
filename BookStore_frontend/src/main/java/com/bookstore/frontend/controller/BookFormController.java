package com.bookstore.frontend.controller;

import com.bookstore.frontend.components.TagInputField;
import com.bookstore.frontend.controller.strategy.BookFormStrategy;
import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.service.api.MasterDataApiService;
import com.bookstore.frontend.utils.AlertUtils;
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
import java.util.concurrent.CompletableFuture;

public class BookFormController {

    @FXML private Label lblFormTitle;
    @FXML private ImageView imgCover;
    @FXML private TextField txtTitle;

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

    private BookFormStrategy formStrategy;

    private final MasterDataApiService masterDataApi = new MasterDataApiService();

    private final Map<String, Long> masterAuthors = new HashMap<>();
    private final Map<String, Long> masterCategories = new HashMap<>();
    private final Map<String, Long> masterPublishers = new HashMap<>();

    @FXML
    public void initialize() {
        btnSave.setDisable(true);
        cbPublisher.setPromptText("Đang tải dữ liệu...");

        masterDataApi.getAllPublishers().thenAccept(list -> Platform.runLater(() -> {
            list.forEach(p -> masterPublishers.put(p.getName(), p.getId()));
            cbPublisher.getItems().addAll(masterPublishers.keySet());
            cbPublisher.setPromptText("Chọn nhà xuất bản");
            btnSave.setDisable(false);
        }));

        tagInputAuthor.setSearchAsyncCallback(keyword ->
                masterDataApi.searchAuthors(keyword).thenApply(dtoList -> {
                    List<String> names = new ArrayList<>();
                    for (var dto : dtoList) {
                        masterAuthors.put(dto.getName(), dto.getId());
                        names.add(dto.getName());
                    }
                    return names;
                })
        );

        tagInputCategory.setSearchAsyncCallback(keyword ->
                masterDataApi.searchCategories(keyword).thenApply(dtoList -> {
                    List<String> names = new ArrayList<>();
                    for (var dto : dtoList) {
                        masterCategories.put(dto.getName(), dto.getId());
                        names.add(dto.getName());
                    }
                    return names;
                })
        );
    }

    public void setBook(BookModel book, BookFormStrategy strategy) {
        this.currentBook = book;
        this.formStrategy = strategy;

        if (book.getTitle() != null) txtTitle.setText(book.getTitle());
        if (book.getDescription() != null) txtDescription.setText(book.getDescription());
        if (book.getPublisherName() != null) cbPublisher.setValue(book.getPublisherName());

        if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
            try {
                imgCover.setImage(new Image(book.getImageUrl(), true));
            } catch (Exception ignored) {}
        }

        List<String> aNames = book.getAuthorNames();
        List<Long> aIds = book.getAuthorIds();
        if (aNames != null && aIds != null && aNames.size() == aIds.size()) {
            for (int i = 0; i < aNames.size(); i++) masterAuthors.put(aNames.get(i), aIds.get(i));
        }
        tagInputAuthor.setTags(aNames);

        List<String> cNames = book.getCategoryNames();
        List<Long> cIds = book.getCategoryIds();
        if (cNames != null && cIds != null && cNames.size() == cIds.size()) {
            for (int i = 0; i < cNames.size(); i++) masterCategories.put(cNames.get(i), cIds.get(i));
        }
        tagInputCategory.setTags(cNames);

        if (this.formStrategy != null) {
            this.formStrategy.setupUI(this, book);
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
        if (txtTitle.getText().trim().isEmpty() || cbPublisher.getValue() == null) {
            showError("Vui lòng nhập Tên sách và chọn Nhà xuất bản!");
            return;
        }

        btnSave.setDisable(true);
        btnSave.setText("Đang xử lý...");

        currentBook.setTitle(txtTitle.getText().trim());
        currentBook.setDescription(txtDescription.getText().trim());

        try {
            currentBook.setPrice(Double.parseDouble(txtPrice.getText().trim()));
            currentBook.setQuantity(Integer.parseInt(txtQuantity.getText().trim()));
        } catch (NumberFormatException e) {
            showError("Giá bán và Số lượng phải là chữ số hợp lệ!");
            return;
        }

        String pubName = cbPublisher.getValue();
        Long pubId = masterPublishers.get(pubName);
        if (pubId == null) {
            showError("Nhà xuất bản không hợp lệ!");
            return;
        }
        currentBook.setPublisherName(pubName);
        currentBook.setPublisherId(pubId);

        List<String> selectedAuthors = tagInputAuthor.getTags();
        List<String> selectedCategories = tagInputCategory.getTags();
        currentBook.setAuthorNames(selectedAuthors);
        currentBook.setCategoryNames(selectedCategories);

        CompletableFuture<List<Long>> authorIdsFuture = resolveAuthorIds(selectedAuthors);
        CompletableFuture<List<Long>> categoryIdsFuture = resolveCategoryIds(selectedCategories);

        authorIdsFuture.thenCombine(categoryIdsFuture, (aIds, cIds) -> {
            if (aIds.contains(-1L) || cIds.contains(-1L)) {
                throw new RuntimeException("Không thể cấu hình dữ liệu Master Data trên máy chủ.");
            }
            currentBook.setAuthorIds(aIds);
            currentBook.setCategoryIds(cIds);
            return currentBook;
        }).thenCompose(book -> {
            return formStrategy != null
                    ? formStrategy.handleSave(book, selectedImageFile)
                    : CompletableFuture.completedFuture(false);
        }).thenAccept(success -> Platform.runLater(() -> {
            if (success) {
                this.saveClicked = true;
                ((Stage) btnSave.getScene().getWindow()).close();
            } else {
                showError("Thao tác thất bại. Vui lòng kiểm tra lại trạng thái kết nối!");
            }
        })).exceptionally(ex -> {
            showError(ex.getMessage());
            return null;
        });
    }

    private CompletableFuture<List<Long>> resolveAuthorIds(List<String> names) {
        List<CompletableFuture<Long>> futures = names.stream().map(name -> {
            if (masterAuthors.containsKey(name)) {
                return CompletableFuture.completedFuture(masterAuthors.get(name));
            }
            return masterDataApi.createAuthor(name).thenApply(dto -> {
                if (dto != null) {
                    masterAuthors.put(dto.getName(), dto.getId());
                    return dto.getId();
                }
                return -1L;
            });
        }).toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream().map(CompletableFuture::join).toList());
    }

    private CompletableFuture<List<Long>> resolveCategoryIds(List<String> names) {
        List<CompletableFuture<Long>> futures = names.stream().map(name -> {
            if (masterCategories.containsKey(name)) {
                return CompletableFuture.completedFuture(masterCategories.get(name));
            }
            return masterDataApi.createCategory(name).thenApply(dto -> {
                if (dto != null) {
                    masterCategories.put(dto.getName(), dto.getId());
                    return dto.getId();
                }
                return -1L;
            });
        }).toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream().map(CompletableFuture::join).toList());
    }

    private void showError(String msg) {
        Platform.runLater(() -> {
            btnSave.setDisable(false);
            btnSave.setText(btnSave.getText()); // Giữ nguyên chữ hiển thị hiện tại của luồng
            AlertUtils.show(Alert.AlertType.ERROR, "Hệ Thống", msg);
        });
    }

    @FXML void handleCancel() { ((Stage) btnSave.getScene().getWindow()).close(); }

    public Label getLblFormTitle() { return lblFormTitle; }
    public Button getBtnSave() { return btnSave; }
    public TextField getTxtPrice() { return txtPrice; }
    public TextField getTxtQuantity() { return txtQuantity; }

    public BookModel getCurrentBook() { return currentBook; }
    public File getSelectedImageFile() { return selectedImageFile; }

    public boolean isSaveClicked() { return saveClicked; }
}