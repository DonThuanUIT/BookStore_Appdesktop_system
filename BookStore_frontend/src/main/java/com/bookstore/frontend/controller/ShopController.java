package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.ShopInteractor;
import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.model.ShopModel;
import com.bookstore.frontend.model.dto.Response.CategoryResponseDto;
import com.bookstore.frontend.service.api.ApiClient;
import com.bookstore.frontend.util.CartStore;
import com.bookstore.frontend.utils.AlertUtils;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ShopController {

    @FXML private TextField txtSearch;
    @FXML private TextField txtMinPrice;
    @FXML private TextField txtMaxPrice;
    @FXML private ComboBox<String> cbSort;
    @FXML private FlowPane booksContainer;
    @FXML private Label lblStatus;

    @FXML private TextField txtCategorySearch;
    @FXML private VBox categoriesContainer;

    @FXML private BookDetailSidePanelController bookDetailSidePanelController;

    private ShopModel model;
    private ShopInteractor interactor;
    private List<BookModel> originalBooksList = new ArrayList<>();

    private List<String> allCategoryNames = new ArrayList<>();
    private final Set<String> activeSelectedCategories = new HashSet<>();

    private static final String DEFAULT_COVER_URL = "https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg";

    public ShopController() {
        this.model = new ShopModel();
        this.interactor = new ShopInteractor(this.model);
    }

    @FXML
    public void initialize() {
        setupUI();
        setupRealTimeFilters();
        loadCategoriesFromApi();
        loadInitialData();

        // Bật bộ đàm lắng nghe tín hiệu Real-time SSE từ Backend
        setupRealTimeSync();
    }

    private void setupUI() {
        if (cbSort != null) {
            cbSort.setItems(FXCollections.observableArrayList(
                    "Newest",
                    "Price: Low to High",
                    "Price: High to Low"
            ));
            cbSort.setValue("Newest");
        }
        // Đã xóa bỏ hoàn toàn các logic liên quan đến btnSearchType
    }

    private void loadCategoriesFromApi() {
        ApiClient.getInstance().get("/categories").thenAccept(res -> {
            if (res.statusCode() == 200) {
                try {
                    JsonNode root = ApiClient.getInstance().getMapper().readTree(res.body());
                    List<CategoryResponseDto> dtoList = ApiClient.getInstance().getMapper()
                            .readerForListOf(CategoryResponseDto.class)
                            .readValue(root);

                    allCategoryNames = dtoList.stream().map(CategoryResponseDto::getName).toList();

                    Platform.runLater(() -> {
                        renderCategoryCheckboxes("");
                    });
                } catch (Exception e) {
                    System.err.println("Lỗi parse JSON Thể loại: " + e.getMessage());
                }
            }
        });
    }

    private void renderCategoryCheckboxes(String filterText) {
        if (categoriesContainer == null) return;
        categoriesContainer.getChildren().clear();

        String lowerFilter = filterText == null ? "" : filterText.toLowerCase();

        for (String categoryName : allCategoryNames) {
            if (categoryName.toLowerCase().contains(lowerFilter)) {
                CheckBox cb = new CheckBox(categoryName);
                cb.setStyle("-fx-text-fill: #AAAAAA; -fx-padding: 2 0 2 0;");

                cb.setSelected(activeSelectedCategories.contains(categoryName));

                cb.selectedProperty().addListener((obs, old, isSelected) -> {
                    if (isSelected) {
                        activeSelectedCategories.add(categoryName);
                    } else {
                        activeSelectedCategories.remove(categoryName);
                    }
                    executeFilter();
                });

                categoriesContainer.getChildren().add(cb);
            }
        }
    }

    private void loadInitialData() {
        if (lblStatus != null) lblStatus.setText("Loading data...");

        interactor.getBooksPage(0, 50).thenAccept(pageDto -> {
            Platform.runLater(() -> {
                if (pageDto.getContent().isEmpty()) {
                    if (lblStatus != null) lblStatus.setText("There are no books in stock.");
                    return;
                }

                originalBooksList = pageDto.getContent();
                if (lblStatus != null) lblStatus.setText("");
                executeFilter();
            });
        });
    }

    private void setupRealTimeFilters() {
        txtSearch.textProperty().addListener((obs, old, newVal) -> executeFilter());
        txtMinPrice.textProperty().addListener((obs, old, newVal) -> executeFilter());
        txtMaxPrice.textProperty().addListener((obs, old, newVal) -> executeFilter());
        cbSort.valueProperty().addListener((obs, old, newVal) -> executeFilter());

        if (txtCategorySearch != null) {
            txtCategorySearch.textProperty().addListener((obs, old, newVal) -> {
                renderCategoryCheckboxes(newVal);
            });
        }
    }

    private void executeFilter() {
        String keyword = txtSearch.getText() != null ? txtSearch.getText().trim() : "";
        final List<String> selectedCategories = new ArrayList<>(activeSelectedCategories);

        if (lblStatus != null) lblStatus.setText("Searching...");

        if (keyword.isEmpty()) {
            List<BookModel> finalFilteredBooks = interactor.applyClientSideFilters(
                    originalBooksList, "", selectedCategories,
                    parseDoubleSafe(txtMinPrice.getText()), parseDoubleSafe(txtMaxPrice.getText()), cbSort.getValue()
            );
            renderBooks(finalFilteredBooks);
            if (lblStatus != null) lblStatus.setText("Found " + finalFilteredBooks.size() + " books.");
            return;
        }

        interactor.searchBooksFromBackend(keyword).thenAccept(booksFromBackend -> {
            Platform.runLater(() -> {
                List<BookModel> finalFilteredBooks = interactor.applyClientSideFilters(
                        booksFromBackend, "", selectedCategories,
                        parseDoubleSafe(txtMinPrice.getText()), parseDoubleSafe(txtMaxPrice.getText()), cbSort.getValue()
                );

                renderBooks(finalFilteredBooks);

                if (lblStatus != null) {
                    if (finalFilteredBooks.isEmpty()) {
                        lblStatus.setText("No books found matching your criteria.");
                    } else {
                        lblStatus.setText("Found " + finalFilteredBooks.size() + " books.");
                    }
                }
            });
        });
    }

    private void renderBooks(List<BookModel> books) {
        booksContainer.getChildren().clear();
        if (books.isEmpty()) return;

        for (BookModel book : books) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bookstore/frontend/view/components/BookCard.fxml"));
                Node cardNode = loader.load();

                String formattedPrice = String.format("%,.0f đ", book.getPrice());
                String imageUrl = (book.getImageUrl() != null && !book.getImageUrl().isBlank())
                        ? book.getImageUrl()
                        : DEFAULT_COVER_URL;

                BookCardController cardController = loader.getController();

                cardController.setBookData(book.getTitle(), book.getFormattedAuthors(), formattedPrice, imageUrl);

                cardController.setCallbacks(
                        () -> {
                            if (bookDetailSidePanelController != null) {
                                bookDetailSidePanelController.setBookDetailDataAndShow(book);
                            }
                        },
                        () -> AlertUtils.promptQuantityForCart(book.getTitle())
                                .ifPresent(qty -> CartStore.getInstance().addBook(book, qty))
                );

                booksContainer.getChildren().add(cardNode);
            } catch (Exception e) {
                System.err.println("Error rendering Shop book card:" + book.getTitle());
            }
        }
    }

    private Double parseDoubleSafe(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void setupRealTimeSync() {
        ApiClient.getInstance().onBookUpdated(updatedBook -> {
            Platform.runLater(() -> {
                boolean found = false;
                for (int i = 0; i < originalBooksList.size(); i++) {
                    if (originalBooksList.get(i).getId().equals(updatedBook.getId())) {
                        originalBooksList.set(i, updatedBook);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    originalBooksList.add(0, updatedBook);
                }
                executeFilter();
            });
        });

        ApiClient.getInstance().onBookDeleted(bookId -> {
            Platform.runLater(() -> {
                originalBooksList.removeIf(b -> b.getId().equals(bookId));
                executeFilter();
            });
        });
    }
}