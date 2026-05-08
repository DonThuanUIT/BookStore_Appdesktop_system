package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.ShopInteractor;
import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.model.ShopModel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

import java.util.ArrayList;
import java.util.List;

public class ShopController {

    @FXML private TextField txtSearch;
    @FXML private TextField txtMinPrice;
    @FXML private TextField txtMaxPrice;
    @FXML private ComboBox<String> cbSort;
    @FXML private FlowPane booksContainer;
    @FXML private Label lblStatus;

    private ShopModel model;
    private ShopInteractor interactor;
    private List<BookModel> originalBooksList = new ArrayList<>();

    // Ảnh bìa mặc định khi sách chưa được cập nhật ảnh
    private static final String DEFAULT_COVER_URL = "https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg";

    public ShopController() {
        this.model = new ShopModel();
        this.interactor = new ShopInteractor(this.model);
    }

    @FXML
    public void initialize() {
        setupUI();
        setupRealTimeFilters();
        loadInitialData();
    }

    private void setupUI() {
        cbSort.setItems(FXCollections.observableArrayList(
                "Newest",
                "Price: Low to High",
                "Price: High to Low"
        ));
        cbSort.setValue("Newest");
    }

    private void setupRealTimeFilters() {
        txtSearch.textProperty().addListener((obs, oldText, newText) -> executeFilter());
        txtMinPrice.textProperty().addListener((obs, oldText, newText) -> executeFilter());
        txtMaxPrice.textProperty().addListener((obs, oldText, newText) -> executeFilter());
        cbSort.valueProperty().addListener((obs, oldVal, newVal) -> executeFilter());
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

    private void executeFilter() {
        String keyword = txtSearch.getText();
        String sortType = cbSort.getValue();
        Double minPrice = parseDoubleSafe(txtMinPrice.getText());
        Double maxPrice = parseDoubleSafe(txtMaxPrice.getText());

        List<BookModel> filteredBooks = interactor.applyClientSideFilters(
                originalBooksList, keyword, null, minPrice, maxPrice, sortType
        );
        renderBooks(filteredBooks);
    }

    private void renderBooks(List<BookModel> books) {
        booksContainer.getChildren().clear();
        if (books.isEmpty()) return;

        for (BookModel book : books) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bookstore/frontend/view/components/BookCard.fxml"));
                Node cardNode = loader.load();

                String formattedPrice = String.format("$%.2f", book.getPrice());
                String imageUrl = (book.getImageUrl() != null && !book.getImageUrl().isBlank())
                        ? book.getImageUrl()
                        : DEFAULT_COVER_URL;

                BookCardController cardController = loader.getController();
                cardController.setBookData(book.getTitle(), book.getAuthorName(), formattedPrice, imageUrl);

                cardController.setCallbacks(
                        () -> System.out.println("Opening book details ID: " + book.getId()),
                        () -> System.out.println("Added to cart: " + book.getTitle())
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
}